import os
import json

KAFKA_BROKER = os.getenv('KAFKA_BROKER', 'localhost:9092')
KAFKA_INPUT_TOPIC = os.getenv('INPUT_TOPIC', 'recommendation.analysis.input')
KAFKA_OUTPUT_TOPIC = os.getenv('OUTPUT_TOPIC', 'recommendation.analysis.output')
DLQ_TOPIC = os.getenv('DLQ_TOPIC_RECOMMENDATION', 'recommendation.analysis.output-dlq')
GIGACHAT_API_KEY = os.getenv('GIGACHAT_API_KEY', '')

MAX_RETRIES = int(os.getenv('RETRY_MAX_ATTEMPTS', '5'))
RETRY_BACKOFF_MS = int(os.getenv('RETRY_BACKOFF_MS', '1000'))
RETRY_BACKOFF_MULTIPLIER = float(os.getenv('RETRY_BACKOFF_MULTIPLIER', '2.0'))
RETRY_MAX_DELAY_MS = int(os.getenv('RETRY_MAX_DELAY_MS', '60000'))

CHECKLIST_ITEMS_JSON = os.getenv('CHECKLIST_ITEMS', '[]')
CHECKLIST_ITEMS = json.loads(CHECKLIST_ITEMS_JSON)
MAX_TOTAL_PENALTY = sum(item['max_penalty'] for item in CHECKLIST_ITEMS)

# Redis
REDIS_HOST = os.getenv('REDIS_HOST', 'redis')
REDIS_PORT = int(os.getenv('REDIS_PORT', 6379))
REDIS_DB = int(os.getenv('REDIS_DB', 0))
REDIS_RETRY_TTL_SEC = int(os.getenv('REDIS_RETRY_TTL_SEC', 3600))

def get_checklist_text():
    return "\n".join(
        f"{item['code']} - {item['description']} ({item['max_penalty']})"
        for item in CHECKLIST_ITEMS
    )