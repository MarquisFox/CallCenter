import requests
import os
import tempfile

def download_audio(presigned_url: str) -> str:
    resp = requests.get(presigned_url, timeout=30)
    resp.raise_for_status()
    fd, local_path = tempfile.mkstemp(suffix=".audio")
    os.close(fd)
    with open(local_path, "wb") as f:
        f.write(resp.content)
    return local_path