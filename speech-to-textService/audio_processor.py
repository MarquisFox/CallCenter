import whisper
import os
import tempfile
from http_client import download_audio

class AudioProcessor:
    def __init__(self, model_size: str = "small"):
        self.model = whisper.load_model(model_size)

    def process_audio_from_url(self, file_url: str) -> str:

        local_path = None
        try:
            local_path = download_audio(file_url)
            result = self.model.transcribe(local_path)
            return result["text"]
        finally:
            if local_path and os.path.exists(local_path):
                os.remove(local_path)