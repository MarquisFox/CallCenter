import logging
import threading
import signal
from http.server import HTTPServer, BaseHTTPRequestHandler
from kafka_client import KafkaService
from tonality_analyzer import TonalityAnalyzer
from models import InputMessage

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

kafka_service = None

class HealthHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/health":
            self.send_response(200)
            self.end_headers()
            self.wfile.write(b"OK")
        else:
            self.send_response(404)

def run_healthcheck(port=8080):
    server = HTTPServer(('0.0.0.0', port), HealthHandler)
    server.serve_forever()

def shutdown_handler(signum, frame):
    logger.info(f"Received signal {signum}, shutting down...")
    if kafka_service:
        kafka_service.stop()
    threading.Timer(5.0, lambda: exit(0)).start()

def main():
    global kafka_service
    health_thread = threading.Thread(target=run_healthcheck, daemon=True)
    health_thread.start()

    signal.signal(signal.SIGINT, shutdown_handler)
    signal.signal(signal.SIGTERM, shutdown_handler)

    kafka_service = KafkaService()
    analyzer = TonalityAnalyzer()

    def process_message(msg: InputMessage) -> str:
        return analyzer.analyze(msg.transcript)

    try:
        kafka_service.run(process_message)
    except Exception as e:
        logger.critical(f"Fatal error: {e}")
        if kafka_service:
            kafka_service.stop()

if __name__ == "__main__":
    main()