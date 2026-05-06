# Speech-to-Text Service (STT)

## Назначение
– Читает `call.registration`, получает `fileUrl` (presigned URL).
– Скачивает аудио по URL и сохраняет во временный файл.
– Загружает модель Whisper (размер задаётся переменной).
– Транскрибирует аудио в текст.
– Отправляет результат в `transcription.completed`.

## Технологии
- Python 3.10
- `openai-whisper` (модель распознавания)
- `kafka-python` (consumer/producer с ручным коммитом)
- `requests` (загрузка по presigned URL)
- `redis` (хранение счётчиков ретраев)


## Механизм ретраев и DLQ
При ошибке (недоступность URL, таймаут, ошибка whisper) счётчик попыток хранится в Redis. После исчерпания `RETRY_MAX_ATTEMPTS` сообщение отправляется в DLQ.
