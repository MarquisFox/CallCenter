from pydantic import BaseModel
from typing import Literal

class InputMessage(BaseModel):
    callId: str
    transcript: str


class OutputMessage(BaseModel):
    callId: str
    tonality: str