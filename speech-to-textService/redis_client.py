import redis
import config
import logging

logger = logging.getLogger(__name__)

_redis_client = None

def get_redis_client():
    global _redis_client
    if _redis_client is None:
        _redis_client = redis.Redis(
            host=config.REDIS_HOST,
            port=config.REDIS_PORT,
            db=config.REDIS_DB,
            decode_responses=True
        )

        try:
            _redis_client.ping()
        except Exception as e:
            logger.error(f"Redis connection failed: {e}")
            raise
    return _redis_client