from rapidfuzz import fuzz

def calculate_similarity(text1: str, text2: str) -> float:
    score = fuzz.ratio(text1, text2)
    return round(score / 100, 2)