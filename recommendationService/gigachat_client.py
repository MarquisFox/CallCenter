from gigachat import GigaChat
import config
import json
import logging
from models import OutputMessage, ChecklistItem

logger = logging.getLogger(__name__)


class InvalidGigaChatResponseError(ValueError):
    
    pass


class GigaChatService:
    def __init__(self):
        if not config.GIGACHAT_API_KEY:
            raise ValueError("GIGACHAT_API_KEY не установлен!")
        self.client = GigaChat(credentials=config.GIGACHAT_API_KEY, verify_ssl_certs=False)
        self.checklist_items = config.CHECKLIST_ITEMS
        self.max_total_penalty = config.MAX_TOTAL_PENALTY

    def build_prompt(self, dialog_text: str) -> str:
        checklist_text = config.get_checklist_text()
        prompt = f"""
Ты — система оценки качества диалогов менеджеров. Твоя задача — проанализировать диалог и вернуть ответ ТОЛЬКО в формате JSON.

### Чек-лист (код - описание, максимальный штраф):
{checklist_text}

### Диалог:
{dialog_text}

### Требования к ответу:
- Для каждого пункта чек-листа (код {', '.join(item['code'] for item in self.checklist_items)}) укажи:
  * completed (true/false) – выполнен ли пункт
  * penalty (целое число от 0 до max_penalty) – начисленный штраф (0 если выполнен)
  * recommendation (строка) – рекомендация по улучшению (пустая строка, если выполнен)
- Определи общую оценку менеджера:
  * rating (число от 1.0 до 5.0) – десятичная дробь, шаг 0.1
- Не добавляй никакого текста до или после JSON.
- Используй только двойные кавычки.

Пример ответа (для чек-листа из {len(self.checklist_items)} пунктов):
{{
  "rating": 4.2,
  "items": [
    {{"code": "1", "completed": true, "penalty": 0, "recommendation": ""}},
    {{"code": "2", "completed": false, "penalty": 3, "recommendation": "Назовите компанию"}},
    ...
  ]
}}
"""
        return prompt

    def parse_gigachat_response(self, raw_response: str, call_id: str) -> OutputMessage:
        if not raw_response or not raw_response.strip():
            raise ValueError("Пустой ответ от GigaChat")

        response = raw_response.strip()

        if response.startswith("```json"):
            response = response[7:].strip()
        if response.startswith("```"):
            response = response[3:].strip()
        if response.endswith("```"):
            response = response[:-3].strip()

        json_start = response.find('{')
        if json_start == -1:
            raise ValueError("В ответе GigaChat не найден JSON")
        json_str = response[json_start:]

        try:
            data = json.loads(json_str)
        except json.JSONDecodeError as e:
            raise ValueError(f"Некорректный JSON от GigaChat: {e}")

        if "rating" not in data or "items" not in data:
            raise InvalidGigaChatResponseError(f"Отсутствуют обязательные поля rating или items для {call_id}")

        rating = float(data["rating"])
        if not (1.0 <= rating <= 5.0):
            logger.warning(f"Rating {rating} вне диапазона, обрезаем до [1,5]")
            rating = max(1.0, min(5.0, rating))

        expected_codes = {item['code'] for item in self.checklist_items}
        items = []
        total_penalty = 0

        for item_data in data["items"]:
            code = item_data.get("code")
            if code not in expected_codes:
                logger.warning(f"Неизвестный код пункта {code}, пропускаем")
                continue

            completed = bool(item_data.get("completed", False))
            penalty = int(item_data.get("penalty", 0))
            max_penalty = next((item['max_penalty'] for item in self.checklist_items if item['code'] == code), 0)

            if penalty < 0:
                logger.warning(f"Отрицательный штраф {penalty} для пункта {code}, обнуляем")
                penalty = 0
            if penalty > max_penalty:
                logger.warning(f"Штраф {penalty} превышает максимум {max_penalty} для пункта {code}, обрезаем")
                penalty = max_penalty

            recommendation = str(item_data.get("recommendation", ""))

            items.append(ChecklistItem(
                code=code,
                completed=completed,
                penalty=penalty,
                recommendation=recommendation
            ))
            total_penalty += penalty

        if len(items) != len(expected_codes):
            raise InvalidGigaChatResponseError(
                f"Неполный набор пунктов: ожидалось {len(expected_codes)}, получено {len(items)} для {call_id}"
            )

        if self.max_total_penalty > 0:
            computed_error_rate = total_penalty / self.max_total_penalty
            computed_error_rate = round(computed_error_rate, 2)
        else:
            logger.error("max_total_penalty = 0! Проверьте CHECKLIST_ITEMS в конфиге")
            computed_error_rate = 0.0

        logger.info(
            f"Вычислен errorRate: {computed_error_rate} "
            f"(сумма penalty: {total_penalty}/{self.max_total_penalty})"
        )

        return OutputMessage(
            callId=call_id,
            rating=rating,
            errorRate=computed_error_rate,
            items=items
        )

    def analyze_dialog(self, dialog_text: str, call_id: str) -> OutputMessage:
        if not dialog_text or len(dialog_text.strip()) < 20:
            logger.error(f"Диалог {call_id} слишком короткий (менее 20 символов)")
            raise ValueError(f"Диалог слишком короткий для анализа: {call_id}")

        prompt = self.build_prompt(dialog_text)
        try:
            response = self.client.chat(prompt)
        except Exception as e:
            logger.error(f"Ошибка вызова GigaChat для {call_id}: {e}")
            raise Exception(f"Ошибка вызова GigaChat: {e}")

        if not response or not response.choices:
            logger.error(f"GigaChat вернул пустой ответ для {call_id}")
            raise Exception("GigaChat вернул пустой ответ")

        raw_content = response.choices[0].message.content
        return self.parse_gigachat_response(raw_content, call_id)
