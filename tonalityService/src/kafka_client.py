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
            value_serializer=lambda v: json.dumps(v, ensure_ascii=False).encode('utf-8')
        )

        self.consumer = KafkaConsumer(
            config.INPUT_TOPIC,
            bootstrap_servers=config.KAFKA_BROKER,
            group_id="tonality_group",
            auto_offset_reset='earliest',
            enable_auto_commit=False,
            key_deserializer=lambda x: x.decode('utf-8') if x else None,
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )

        self.output_topic = config.OUTPUT_TOPIC
        self.dlq_topic = config.DLQ_TOPIC

        self.redis = get_redis_client()
        self.running = True

    def _get_retry_key(self, call_id: str) -> str:
        return f"retry:tonality:{self.consumer.topics}:{call_id}"

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
        while self.running:
            msgs = self.consumer.poll(timeout_ms=1000)
            for topic_partition, records in msgs.items():
                for msg in records:
                    if not self.running:
                        break
                    call_id = msg.key
                    if call_id is None:
                        try:
                            call_id = msg.value.get('callId', 'unknown')
                        except:
                            call_id = 'unknown'

                    try:
                        attempt = self._get_retry_count(call_id)
                        if attempt >= config.MAX_RETRIES:
                            self.consumer.commit()
                            continue

                        input_data = InputMessage(**msg.value)
                        tonality = processor_func(input_data)
                        self.send_result(call_id, tonality)
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
                            logger.error(
                                f"Error processing {call_id}, attempt {attempt}/{config.MAX_RETRIES}, "
                                f"backoff {backoff}s: {e}"
                            )
                            time.sleep(backoff)
                            # не коммитим сообщение
                        else:
                            logger.error(f"Max retries exceeded for {call_id}, sending to DLQ")
                            self._send_to_dlq(msg.value, str(e), call_id)
                            self.consumer.commit()
                            self._clear_retry_count(call_id)

        self.consumer.close()
        self.producer.close()
        logger.info("KafkaService stopped")

    def send_result(self, call_id: str, tonality: str):
        output_message = OutputMessage(callId=call_id, tonality=tonality)
        self.producer.send(
            self.output_topic,
            key=call_id,
            value=output_message.model_dump()
        )
        self.producer.flush()