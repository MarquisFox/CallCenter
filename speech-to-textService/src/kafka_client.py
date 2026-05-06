import json
import time
import logging
from datetime import datetime
from kafka import KafkaConsumer, KafkaProducer
import config
from models import InputMessage, OutputMessage
from redis_client import get_redis_client

logger = logging.getLogger(__name__)

class KafkaService:
    def __init__(self):
        self.producer = KafkaProducer(
            bootstrap_servers=config.KAFKA_BROKER,
            key_serializer=lambda k: k.encode('utf-8') if k else None,
            value_serializer=lambda v: json.dumps(v, ensure_ascii=False).encode("utf-8"),
        )

        self.consumer = KafkaConsumer(
            config.KAFKA_INPUT_TOPIC,
            bootstrap_servers=config.KAFKA_BROKER,
            group_id="speech_transcription_group",
            auto_offset_reset="earliest",
            enable_auto_commit=False,
            key_deserializer=lambda k: k.decode('utf-8') if k else None,
            value_deserializer=lambda m: json.loads(m.decode("utf-8")),
        )

        self.input_topic = config.KAFKA_INPUT_TOPIC
        self.output_topic = config.KAFKA_OUTPUT_TOPIC
        self.dlq_topic = config.DLQ_TOPIC

        self.redis = get_redis_client()
        self.running = True

    def _get_retry_key(self, call_id: str) -> str:
        return f"retry:stt:{self.input_topic}:{call_id}"

    def _get_retry_count(self, call_id: str) -> int:
        key = self._get_retry_key(call_id)
        val = self.redis.get(key)
        return int(val) if val else 0

    def _increment_retry_count(self, call_id: str) -> int:
        key = self._get_retry_key(call_id)
        new_count = self.redis.incr(key)
        self.redis.expire(key, config.REDIS_RETRY_TTL_SEC)
        return new_count

    def _clear_retry_count(self, call_id: str):
        key = self._get_retry_key(call_id)
        self.redis.delete(key)

    def _send_to_dlq(self, original_message, error_msg, call_id):
        dlq_payload = {
            "original_message": original_message,
            "error": error_msg,
            "timestamp": datetime.utcnow().isoformat(),
            "call_id": call_id
        }
        self.producer.send(self.dlq_topic, key=call_id, value=dlq_payload)
        self.producer.flush()
        logger.warning(f"Sent to DLQ: {call_id}")

    def stop(self):
        logger.info("Stopping KafkaService...")
        self.running = False

    def run(self, processor_func):
        """
        Основной цикл обработки сообщений с ретраями и DLQ.
        Состояние ретраев хранится в Redis.
        """
        while self.running:
            msgs = self.consumer.poll(timeout_ms=1000)
            for topic_partition, records in msgs.items():
                for msg in records:
                    if not self.running:
                        break
                    call_id = msg.key
                    if call_id is None:
                        call_id = msg.value.get("callId", "unknown")
                    try:
                        # Проверяем, не исчерпаны ли уже попытки (чтобы не обрабатывать заново)
                        attempt = self._get_retry_count(call_id)
                        if attempt >= config.MAX_RETRIES:
                            # Пропускаем, уже в DLQ (но на всякий случай)
                            self.consumer.commit()
                            continue

                        input_msg = InputMessage(**msg.value)
                        transcript = processor_func(input_msg.fileUrl)
                        output = OutputMessage(callId=input_msg.callId, transcript=transcript)
                        self.producer.send(self.output_topic, key=output.callId, value=output.model_dump())
                        self.producer.flush()
                        self.consumer.commit()
                        self._clear_retry_count(call_id)
                        logger.info(f"Processed {call_id} successfully")
                    except Exception as e:
                        attempt = self._increment_retry_count(call_id)
                        if attempt <= config.MAX_RETRIES:
                            backoff = min(
                                config.RETRY_BACKOFF_MS * (config.RETRY_BACKOFF_MULTIPLIER ** (attempt - 1)),
                                config.RETRY_MAX_DELAY_MS
                            ) / 1000.0
                            logger.error(f"Error processing {call_id}, attempt {attempt}/{config.MAX_RETRIES}, "
                                         f"backoff {backoff}s: {e}")
                            time.sleep(backoff)
                        else:
                            logger.error(f"Max retries exceeded for {call_id}, sending to DLQ")
                            self._send_to_dlq(msg.value, str(e), call_id)
                            self.consumer.commit()
                            self._clear_retry_count(call_id)

        self.consumer.close()
        self.producer.close()
        logger.info("KafkaService stopped")