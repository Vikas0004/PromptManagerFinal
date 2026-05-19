from pydantic import BaseModel

class PredictRequest(BaseModel):
    text: str

class PredictResponse(BaseModel):
    predictedTool: str
    confidence: float

class EmbedRequest(BaseModel):
    text: str

class EmbedResponse(BaseModel):
    embedding: list[float]