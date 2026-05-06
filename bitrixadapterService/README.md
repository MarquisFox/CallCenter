# BitrixAdapter – API Gateway

## Назначение
– Принимает HTTP‑запросы от CRM и фронта.
– Валидирует и нормализует данные о звонке, отправляет в Kafka (`call.registration`).
– Проксирует запросы на `statistics-service` (получение списков, отчётов).

## Технологии
- Java 17, Spring Boot 3.4.4
- Spring WebFlux (WebClient для проксирования)
- Spring Kafka (продюсер)
- Lombok, MapStruct
- OpenAPI (Swagger UI)


## Эндпоинты Swagger
`http://localhost:8081/swagger-ui.html`

