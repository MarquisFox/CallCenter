# Statistics Service – Data Service

## Назначение
– Читает Kafka топики:
  - `call.registration` – создание звонка и менеджера.
  - `tonality.analysis.output` – обновление тональности.
  - `recommendation.analysis.output` – обновление рейтинга, ошибок, пунктов чек-листа.
– Сохраняет данные в PostgreSQL (таблицы: `manager`, `call`, `tonality`, `status`, `checklist_item`, `call_checklist_result`, `manager_stats`).
– Предоставляет HTTP API для получения отчётов (используется BitrixAdapter).
– Кеширует ответы в Redis (TTL управляется).
– Реализует production‑ready механизмы: ручной коммит смещений, ретраи с exponential backoff, DLQ.

## Технологии
- Java 17, Spring Boot 3.4.4
- Spring Data JPA (Hibernate), Liquibase (миграции)
- Spring Kafka (консьюмеры с `@RetryableTopic`)
- Spring Cache (Redis – `@Cacheable`, `@CacheEvict`)
- PostgreSQL, Redis


## Эндпоинты (внутренние, доступны только через BitrixAdapter)
- `GET /api/v1/calls/get-all-calls?page=&size=`
- `GET /api/v1/dashboard/aggregated?startDate=&endDate=&managerIds=`
- `GET /api/v1/managers/get-managers-with-error-rate`
- `GET /api/v1/managers/get-manager-details?id=&startDate=&endDate=&tonalityIds=`

## Статусы звонков
- `created` – звонок создан.
- `sentiment_done` – тональность получена.
- `gigachat_done` – оценка Gigachat получена.
- `completed` – оба результата получены.

## Кеширование
- `managersErrorRate` – список менеджеров с errorRate (TTL 10 мин).
- `managerDetails` – детали менеджера (TTL 15 мин).
- `dashboardAggregated` – агрегаты дашборда (TTL 5 мин).

## DLQ
При необрабатываемых ошибках сообщение отправляется в DLQ-топик: `*.dlq`