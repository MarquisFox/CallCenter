from pydantic import BaseModel
from typing import List, Literal


class InputMessage(BaseModel):
    callId: str
    transcript: str

class ChecklistItem(BaseModel):
    code: Literal["1", "2", "3", "4", "5"]
    completed: bool
    penalty: int
    recommendation: str

class OutputMessage(BaseModel):
    callId: str
    rating: float  # 1-5
    errorRate: float  # 0-1
    items: List[ChecklistItem]
