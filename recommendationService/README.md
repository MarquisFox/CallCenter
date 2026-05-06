# Recommendation Service – Оценка по чек-листу (GigaChat)

## Назначение
– Читает `recommendation.analysis.input` (транскрипт).
– Формирует промт с актуальным чек-листом (переменная `CHECKLIST_ITEMS`).
– Вызывает GigaChat API, получает JSON с оценкой пунктов и рейтингом.
– Вычисляет `errorRate` как `сумма penalty / максимальный штраф`.
– Отправляет результат в `recommendation.analysis.output`.

## Технологии
- Python 3.10
- `gigachat` (SDK для GigaChat)
- `kafka-python`, `redis`, `requests`

## Особенности
- Валидирует полноту ответа (должны быть все пункты чек-листа).
- При невалидном ответе (неполный JSON, ошибка парсинга) – ретраи, затем DLQ.
