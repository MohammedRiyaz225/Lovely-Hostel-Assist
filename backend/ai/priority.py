# backend/ai/priority.py
# Priority prediction: Low / Medium / High / Critical
# Uses keyword rules first; falls back to category-based defaults.

import re

# Keywords that escalate priority
CRITICAL_KEYWORDS = [
    "fire", "sparking", "smoke", "explosion", "burst", "flood",
    "stolen", "theft", "emergency", "rape", "assault", "eve-teasing",
    "collapse", "fall", "fell", "accident", "unconscious", "bleeding",
    "no electricity entire", "no water entire", "insect in food",
    "mold in food", "rat", "rodent", "expired", "gas leak",
    "bed bug", "fire exit", "fire extinguisher", "exposed wire",
    "wire exposed", "electrical fire", "water cut off since",
    "boiler broken", "no water since"
]

HIGH_KEYWORDS = [
    "not working", "broken", "damaged", "repair", "since yesterday",
    "since morning", "since last night", "leaking", "blocked",
    "flooding", "overflow", "stagnant", "pest", "cockroach", "ant",
    "dirty", "smell", "foul", "unsafe", "stolen", "lock broken",
    "security guard absent", "outsider", "mold", "crack", "loose",
    "railing loose", "roof leaking", "seeping", "geyser", "no hot water",
    "router down", "wifi down", "internet down", "no signal",
    "door broken"
]

MEDIUM_KEYWORDS = [
    "slow", "poor quality", "noise", "vibrating", "flicker",
    "flickering", "disconnecting", "warm", "insufficient", "delay",
    "late", "not cleaned", "overflowing dustbin", "stale"
]

# Default priorities per category when no strong keyword matches
CATEGORY_DEFAULTS = {
    "Plumbing": "High",
    "Electrical": "High",
    "Cleaning": "Medium",
    "Food": "Medium",
    "Internet/WiFi": "Medium",
    "Furniture": "Medium",
    "Security": "High",
    "Maintenance": "Medium",
    "Other": "Low",
}


def predict_priority(text: str, category: str = "Other") -> str:
    """
    Returns the predicted priority for a complaint.
    Keyword rules take precedence over category defaults.
    """
    lower = text.lower()

    for kw in CRITICAL_KEYWORDS:
        if kw in lower:
            return "Critical"

    for kw in HIGH_KEYWORDS:
        if kw in lower:
            return "High"

    for kw in MEDIUM_KEYWORDS:
        if kw in lower:
            return "Medium"

    return CATEGORY_DEFAULTS.get(category, "Low")
