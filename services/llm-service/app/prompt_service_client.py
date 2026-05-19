import requests

import os
from dotenv import load_dotenv

load_dotenv()

PROMPT_SERVICE_URL = os.getenv(
    "PROMPT_SERVICE_URL"
)

def fetch_similar_prompts(
    query: str,
    auth_token: str
):

    response = requests.post(
        f"{PROMPT_SERVICE_URL}/prompts/semantic-search",
        json={
            "query": query
        },
        headers={
            "Authorization": auth_token
        }
    )

    response.raise_for_status()

    return response.json()