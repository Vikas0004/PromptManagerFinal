from app.prompt_service_client import (
    fetch_similar_prompts
)

from app.ollama_client import (
    improve_prompt_with_ollama
)

def run_prompt_improvement(
    prompt_text,
    auth_token
):

    """
    STEP 1 — FETCH SIMILAR PROMPTS
    """

    similar_prompts = similar_prompts = fetch_similar_prompts(
    prompt_text,
    auth_token
)

    """
    STEP 2 — LIMIT TOP RESULTS
    """

    top_prompts = similar_prompts[:3]

    """
    STEP 3 — CALL OLLAMA
    """

    llm_response = improve_prompt_with_ollama(
        prompt_text,
        top_prompts
    )

    """
    STEP 4 — PARSE RESPONSE
    """

    improved_prompt = llm_response
    explanation = "Prompt enhanced using retrieved semantic context."

    if "EXPLANATION:" in llm_response:

        parts = llm_response.split(
            "EXPLANATION:"
        )

        improved_prompt = (
            parts[0]
            .replace("IMPROVED_PROMPT:", "")
            .strip()
        )

        explanation = parts[1].strip()

    """
    STEP 5 — BUILD RESPONSE
    """

    references = []

    for prompt in top_prompts:

        references.append({
            "title": prompt["prompt"]["title"],
            "similarity": prompt["similarityScore"]
        })

    return {
        "improvedPrompt": improved_prompt,
        "explanation": explanation,
        "referencePrompts": references
    }