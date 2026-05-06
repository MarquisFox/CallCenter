# Tonality Service – Анализ тональности

## Назначение
– Читает `tonality.analysis.input` (фактически `transcription.completed`).
– Использует модель FastTextSocialNetwork (Dostoevsky) для анализа тональности.
– Возвращает `positive`, `negative` или `neutral`.
– Отправляет результат в `tonality.analysis.output`.

## Технологии
- Python 3.10
- `dostoevsky` (FastTextSocialNetworkModel)
- `kafka-python`
- `redis`


## Особенности
- Если транскрипт короче 20 символов, выбрасывается исключение (уходит в DLQ).
- Модель загружается при старте, её размер ~1.5 ГБ.

