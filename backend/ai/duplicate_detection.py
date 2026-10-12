# backend/ai/duplicate_detection.py
# Detects duplicate complaints using TF-IDF cosine similarity

import joblib
import numpy as np
from pathlib import Path
from sklearn.metrics.pairwise import cosine_similarity

VECTORIZER_PATH = Path(__file__).parent / "models" / "category_vectorizer.pkl"

DUPLICATE_THRESHOLD = 0.75   # flag as duplicate if similarity >= 75%

_vectorizer = None


def _load_vectorizer():
    global _vectorizer
    if _vectorizer is None:
        if not VECTORIZER_PATH.exists():
            raise FileNotFoundError("Vectorizer not found. Run: python ai/train.py")
        _vectorizer = joblib.load(VECTORIZER_PATH)


async def find_duplicate(new_text: str, block: str, db) -> dict:
    """
    Compares the new complaint against all open complaints from the same block.
    Returns {isDuplicate, duplicateOf, similarityScore} or empty dict on error.
    """
    try:
        _load_vectorizer()
    except FileNotFoundError:
        return {}

    # Fetch open complaints from the same block (exclude CLOSED)
    cursor = db.complaints.find(
        {
            "hostelBlock": block,
            "status": {"$nin": ["CLOSED", "RESOLVED"]}
        },
        {"_id": 1, "title": 1, "description": 1}
    )

    open_complaints = []
    async for doc in cursor:
        text = f"{doc.get('title', '')} {doc.get('description', '')}"
        open_complaints.append({"id": str(doc["_id"]), "text": text})

    if not open_complaints:
        return {"isDuplicate": False, "duplicateOf": None, "similarityScore": 0.0}

    new_vec = _vectorizer.transform([new_text.lower()])
    existing_texts = [c["text"].lower() for c in open_complaints]
    existing_vecs = _vectorizer.transform(existing_texts)

    similarities = cosine_similarity(new_vec, existing_vecs)[0]
    best_idx = int(np.argmax(similarities))
    best_score = float(similarities[best_idx])

    is_dup = best_score >= DUPLICATE_THRESHOLD
    return {
        "isDuplicate": is_dup,
        "duplicateOf": open_complaints[best_idx]["id"] if is_dup else None,
        "similarityScore": round(best_score * 100, 1)  # as percentage
    }
