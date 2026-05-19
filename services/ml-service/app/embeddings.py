from sentence_transformers import SentenceTransformer

import os
from dotenv import load_dotenv

load_dotenv()

MODEL_NAME = os.getenv(
    "MODEL_NAME",
    "sentence-transformers/all-MiniLM-L6-v2"
)

model = SentenceTransformer(
    MODEL_NAME
)

def generate_embedding(text: str):

    embedding = model.encode(text)

    return embedding.tolist()