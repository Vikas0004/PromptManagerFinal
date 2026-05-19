from pydantic import BaseModel

class PromptRequest(BaseModel):
    title: str
    description: str

class CleanResponse(BaseModel):
    cleanedText: str
    duplicateScore: float