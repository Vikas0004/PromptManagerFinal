import requests

import os
from dotenv import load_dotenv

load_dotenv()

OLLAMA_URL = os.getenv("OLLAMA_URL")

OLLAMA_MODEL = os.getenv(
    "OLLAMA_MODEL",
    "llama3"
)

def improve_prompt_with_ollama(
    prompt_text,
    references
):

    context = "\n".join([
        f"- {r['prompt']['title']}: {r['prompt']['description']}"
        for r in references
    ])

    final_prompt = f"""
You are an expert AI prompt engineer.

TASK:
Improve the user's prompt.

RULES:
- Improve clarity
- Add structure
- Add output formatting
- Preserve original intent

USER PROMPT:
{prompt_text}

REFERENCE PROMPTS:
{context}

Return response in this exact format:

IMPROVED_PROMPT:
...

EXPLANATION:
...
"""

    response = requests.post(
        OLLAMA_URL,
        json={
            "model": OLLAMA_MODEL,
            "prompt": final_prompt,
            "stream": False
        }
    )

    response.raise_for_status()

    return response.json()["response"]