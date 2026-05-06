import os
import shutil

def ensure_directory_exists(directory_path: str) -> None:
    os.makedirs(directory_path, exist_ok=True)


def copy_file_for_processing(original_path: str, processing_dir: str) -> str:

    ensure_directory_exists(processing_dir)

    file_name = os.path.basename(original_path)
    processing_path = os.path.join(processing_dir, file_name)

    with open(original_path, 'rb') as src_file:
        with open(processing_path, 'wb') as dst_file:
            shutil.copyfileobj(src_file, dst_file)

    return processing_path