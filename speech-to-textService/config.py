import os

KAFKA_BROKER = os.getenv('KAFKA_BROKER', 'localhost:9092')
KAFKA_INPUT_TOPIC = os.getenv('KAFKA_INPUT_TOPIC', 'call.registration')
KAFKA_OUTPUT_TOPIC = os.getenv('KAFKA_OUTPUT_TOPIC', 'transcription.completed')
DLQ_TOPIC = os.getenv('DLQ_TOPIC_STT', 'call.registration-dlq')

MAX_RETRIES = int(os.getenv('RETRY_MAX_ATTEMPTS', '5'))
RETRY_BACKOFF_MS = int(os.getenv('RETRY_BACKOFF_MS', '1000'))
RETRY_BACKOFF_MULTIPLIER = float(os.getenv('RETRY_BACKOFF_MULTIPLIER', '2.0'))
RETRY_MAX_DELAY_MS = int(os.getenv('RETRY_MAX_DELAY_MS', '60000'))

WHISPER_MODEL_SIZE = os.getenv('WHISPER_MODEL_SIZE', 'small')

# Redis
REDIS_HOST = os.getenv('REDIS_HOST', 'redis')
REDIS_PORT = int(os.getenv('REDIS_PORT', 6379))
REDIS_DB = int(os.getenv('REDIS_DB', 0))
REDIS_RETRY_TTL_SEC = int(os.getenv('REDIS_RETRY_TTL_SEC', 3600))  