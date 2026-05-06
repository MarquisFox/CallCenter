from pydantic import BaseModel
from typing import Optional

class InputMessage(BaseModel):
    callId: str
    managerId: str
    managerName: Optional[str] = None
    managerPosition: Optional[str] = None
    date: str
    duration: Optional[str] = None
    durationSeconds: Optional[int] = None
    fileUrl: str

class OutputMessage(BaseModel):
    callId: str
    transcript: str