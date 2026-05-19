from dotenv import load_dotenv
import os

load_dotenv()

PORT = int(os.getenv("SERVICE_PORT", 8005))

from fastapi import (
    FastAPI,
    Header,
    HTTPException
)
from fastapi.middleware.cors import CORSMiddleware

from app.schemas import(
    ImproveRequest,
    ImproveResponse
)

from app.rag_service import (
    run_prompt_improvement
)

app = FastAPI(
    title="LLM Service",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def health():
    return {
        "status": "UP"
    }

@app.post(
    "/prompt-assistant/improve",
    response_model=ImproveResponse
)
def improve_prompt(
    request: ImproveRequest,
    authorization: str = Header(None)
):

    if not authorization:
        raise HTTPException(
            status_code=401,
            detail="Authorization header missing"
        )

    result = run_prompt_improvement(
        request.promptText,
        authorization
    )

    return ImproveResponse(**result)