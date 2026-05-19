from fastapi import FastAPI
from app.schemas import PromptRequest, CleanResponse
from app.preprocessing import clean_text

app = FastAPI(
    title="Preprocessing Service",
    version="1.0.0"
)

@app.get("/health")
def health():
    return {"status": "UP"}

@app.post("/clean", response_model=CleanResponse)
def clean_prompt(request: PromptRequest):

    combined = f"{request.title} {request.description}"

    cleaned = clean_text(combined)

    return CleanResponse(
        cleanedText=cleaned,
        duplicateScore=0.0
    )