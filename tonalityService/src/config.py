import os

KAFKA_BROKER = os.getenv('KAFKA_BROKER', 'localhost:9092')
INPUT_TOPIC = os.getenv('INPUT_TOPIC', 'tonality.analysis.input')
OUTPUT_TOPIC = os.getenv('OUTPUT_TOPIC', 'tonality.analysis.output')
DLQ_TOPIC = os.getenv('DLQ_TOPIC_TONALITY', 'tonality.analysis.output-dlq')
MODEL_PATH = os.getenv('TONALITY_MODEL_PATH', 'fasttext-social-network-model.bin')

MAX_RETRIES = int(os.getenv('RETRY_MAX_ATTEMPTS', '5'))
RETRY_BACKOFF_MS = int(os.getenv('RETRY_BACKOFF_MS', '1000'))
RETRY_BACKOFF_MULTIPLIER = float(os.getenv('RETRY_BACKOFF_MULTIPLIER', '2.0'))
RETRY_MAX_DELAY_MS = int(os.getenv('RETRY_MAX_DELAY_MS', '60000'))

# Redis
REDIS_HOST = os.getenv('REDIS_HOST', 'redis')
REDIS_PORT = int(os.getenv('REDIS_PORT', 6379))
REDIS_DB = int(os.getenv('REDIS_DB', 2))  # отдельная БД для тональности
REDIS_RETRY_TTL_SEC = int(os.getenv('REDIS_RETRY_TTL_SEC', 3600))