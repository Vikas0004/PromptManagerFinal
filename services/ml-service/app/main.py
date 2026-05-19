from fastapi import FastAPI

from app.schemas import (
    PredictRequest,
    PredictResponse,
    EmbedRequest,
    EmbedResponse
)

from app.predictor import predict_tool
from app.embeddings import generate_embedding

app = FastAPI(
    title="ML Service",
    version="1.0.0"
)

@app.get("/health")
def health():
    return {"status": "UP"}

@app.post("/predict", response_model=PredictResponse)
def predict(request: PredictRequest):

    result = predict_tool(request.text)

    return PredictResponse(**result)

@app.post("/embed", response_model=EmbedResponse)
def embed(request: EmbedRequest):

    embedding = generate_embedding(request.text)

    return EmbedResponse(
        embedding=embedding
    )