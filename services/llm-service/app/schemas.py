from pydantic import BaseModel
from typing import List

class ImproveRequest(BaseModel):
    promptText: str

class ReferencePrompt(BaseModel):
    title: str
    similarity: float

class ImproveResponse(BaseModel):
    improvedPrompt: str
    explanation: str
    referencePrompts: List[ReferencePrompt]