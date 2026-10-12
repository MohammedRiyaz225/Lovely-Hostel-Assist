# backend/ai/sentiment.py
# Keyword-based sentiment analysis (no external model needed)
# Returns: Positive | Neutral | Negative, and a -1.0 to +1.0 score

POSITIVE_WORDS = [
    "resolved", "fixed", "working", "clean", "good", "great",
    "excellent", "thank", "happy", "satisfied", "better", "improved",
    "appreciate", "done", "sorted"
]

NEGATIVE_WORDS = [
    "broken", "damaged", "dirty", "smelly", "leaking", "not working",
    "not cleaned", "bad", "terrible", "awful", "horrible", "worst",
    "urgent", "critical", "emergency", "unsafe", "dangerous", "disgusting",
    "infestation", "stolen", "unacceptable", "pathetic", "poor",
    "complaint", "problem", "issue", "fault", "error", "fail", "failure",
    "no water", "no electricity", "no internet", "no wifi", "flood",
    "cockroach", "rat", "stale", "expired", "mold", "mould", "fire",
    "smoke", "spark", "loose", "crack", "broken", "burst"
]


def analyze_sentiment(text: str) -> dict:
    """
    Returns a dict with 'label' (Positive/Neutral/Negative) and 'score'.
    Score range: -1.0 (very negative) to +1.0 (very positive).
    """
    lower = text.lower()

    pos_hits = sum(1 for w in POSITIVE_WORDS if w in lower)
    neg_hits = sum(1 for w in NEGATIVE_WORDS if w in lower)

    total = pos_hits + neg_hits
    if total == 0:
        return {"label": "Neutral", "score": 0.0}

    raw_score = (pos_hits - neg_hits) / total  # -1 to +1

    if raw_score > 0.2:
        label = "Positive"
    elif raw_score < -0.2:
        label = "Negative"
    else:
        label = "Neutral"

    return {"label": label, "score": round(raw_score, 2)}
