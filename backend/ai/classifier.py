# backend/ai/classifier.py
# Category classification using TF-IDF + Logistic Regression

import joblib
from pathlib import Path

MODEL_PATH = Path(__file__).parent / "models" / "category_model.pkl"
VECTORIZER_PATH = Path(__file__).parent / "models" / "category_vectorizer.pkl"

CATEGORIES = [
    "Plumbing", "Electrical", "Cleaning", "Food",
    "Internet/WiFi", "Furniture", "Security", "Maintenance", "Other"
]

# Lazy-load the trained model so startup is fast
_model = None
_vectorizer = None


def _load():
    global _model, _vectorizer
    if _model is None:
        if not MODEL_PATH.exists():
            raise FileNotFoundError(
                "Category model not found. Run: python ai/train.py"
            )
        _model = joblib.load(MODEL_PATH)
        _vectorizer = joblib.load(VECTORIZER_PATH)


def predict_category(text: str) -> str:
    """Returns the most likely complaint category for the given text."""
    _load()
    vec = _vectorizer.transform([text.lower()])
    return _model.predict(vec)[0]
