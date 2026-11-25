import re
from typing import Tuple, List, Optional
from app.models import AiDto, DuplicateDto

PLUMBING_KEYWORDS = ["water", "leak", "tap", "pipe", "drain", "sink", "flush", "toilet", "geyser", "shower", "clogged", "overflow", "basin", "drip", "sewage"]
ELECTRICAL_KEYWORDS = ["light", "fan", "switch", "power", "socket", "short circuit", "spark", "fuse", "ac", "wire", "voltage", "blackout", "bulb", "appliances", "tripping", "heater"]
CLEANING_KEYWORDS = ["dirty", "trash", "dustbin", "sweep", "mop", "smell", "garbage", "washroom dirty", "corridor", "pest", "cockroach", "litter", "insect", "unhygienic", "stink"]
WIFI_KEYWORDS = ["wifi", "wi-fi", "internet", "router", "connection", "lan", "slow speed", "disconnect", "signal", "dns", "portal", "ethernet", "network", "bandwidth", "ip"]
FOOD_KEYWORDS = ["mess", "food", "dinner", "lunch", "breakfast", "meal", "caterer", "uncooked", "stale", "taste", "hygiene", "roti", "curry", "water cooler", "insect in food"]
FURNITURE_KEYWORDS = ["bed", "chair", "table", "desk", "cupboard", "almirah", "wardrobe", "mattress", "drawer", "hinge", "door handle", "cot", "shelf"]
SECURITY_KEYWORDS = ["theft", "stolen", "lock", "key", "stranger", "guard", "gate", "cctv", "unauthorized", "trespass", "window latch", "harassment", "lost"]

CRITICAL_KEYWORDS = ["fire", "spark", "short circuit", "shock", "flood", "severe leak", "smoke", "theft", "emergency", "danger", "burst", "gas leak", "burning smell"]
HIGH_KEYWORDS = ["no water", "blackout", "geyser broken", "toilet choked", "3 days", "three days", "several days", "urgent", "exam", "broken lock", "overflowing", "immediately"]
LOW_KEYWORDS = ["paint", "squeak", "loose knob", "scratch", "minor", "dim light", "slow", "suggestion"]

NEGATIVE_KEYWORDS = ["terrible", "worst", "unbearable", "frustrated", "pathetic", "disgusting", "nobody fixed", "again", "danger", "ruined", "urgently", "fed up", "broken", "dirty", "angry"]
POSITIVE_KEYWORDS = ["thanks", "kindly", "please", "appreciate", "helpful", "good", "resolve soon"]

DEPARTMENT_MAP = {
    "PLUMBING": "LPU Estate Maintenance (Plumbing Division)",
    "ELECTRICAL": "LPU Electrical Power & Substations",
    "CLEANING": "LPU Housekeeping & DSW Sanitation",
    "WIFI": "LPU NetOps Campus IT Division",
    "FOOD": "LPU Hostel Mess & Catering Committee",
    "FURNITURE": "LPU Civil & Carpentry Works",
    "SECURITY": "LPU Security & Proctorial Cell",
    "GENERAL": "LPU General Estate Maintenance"
}

def count_matches(text: str, keywords: List[str]) -> int:
    return sum(1 for kw in keywords if kw in text)

def classify_category(text: str) -> Tuple[str, float]:
    lower = text.lower()
    scores = {
        "PLUMBING": count_matches(lower, PLUMBING_KEYWORDS),
        "ELECTRICAL": count_matches(lower, ELECTRICAL_KEYWORDS),
        "CLEANING": count_matches(lower, CLEANING_KEYWORDS),
        "WIFI": count_matches(lower, WIFI_KEYWORDS),
        "FOOD": count_matches(lower, FOOD_KEYWORDS),
        "FURNITURE": count_matches(lower, FURNITURE_KEYWORDS),
        "SECURITY": count_matches(lower, SECURITY_KEYWORDS)
    }
    best_cat, best_score = max(scores.items(), key=lambda x: x[1])
    if best_score > 0:
        confidence = min(0.96, 0.78 + (best_score * 0.05))
        return best_cat, round(confidence, 2)
    return "GENERAL", 0.72

def predict_priority(text: str, category: str) -> str:
    lower = text.lower()
    if any(kw in lower for kw in CRITICAL_KEYWORDS):
        return "CRITICAL"
    if any(kw in lower for kw in HIGH_KEYWORDS) or category == "SECURITY":
        return "HIGH"
    if any(kw in lower for kw in LOW_KEYWORDS):
        return "LOW"
    return "MEDIUM"

def analyze_sentiment(text: str) -> str:
    lower = text.lower()
    neg_score = count_matches(lower, NEGATIVE_KEYWORDS)
    pos_score = count_matches(lower, POSITIVE_KEYWORDS)
    if neg_score >= 2 or any(kw in lower for kw in ["unbearable", "danger", "urgent", "fire", "shock"]):
        return "FRUSTRATED" if neg_score >= 3 else "NEGATIVE"
    if pos_score > neg_score:
        return "POSITIVE"
    return "NEUTRAL"

def tokenize(text: str) -> set:
    words = re.findall(r'\b[a-zA-Z]{3,}\b', text.lower())
    stop_words = {"the", "and", "for", "with", "from", "that", "this", "room", "hostel", "please"}
    return {w for w in words if w not in stop_words}

def detect_duplicate(title: str, description: str, block: str, existing_complaints: List[dict]) -> DuplicateDto:
    current_tokens = tokenize(f"{title} {description}")
    if not current_tokens:
        return DuplicateDto(is_duplicate=False)

    best_match_id = None
    best_similarity = 0.0

    for comp in existing_complaints:
        # Compare within the same block and only active complaints
        if comp.get("block") == block and comp.get("status") in ["PENDING", "IN_PROGRESS"]:
            other_text = f"{comp.get('title', '')} {comp.get('description', '')}"
            other_tokens = tokenize(other_text)
            if not other_tokens:
                continue

            intersection = current_tokens.intersection(other_tokens)
            union = current_tokens.union(other_tokens)
            jaccard = len(intersection) / len(union) if union else 0.0

            if jaccard > best_similarity:
                best_similarity = jaccard
                best_match_id = comp.get("id")

    if best_similarity >= 0.40 and best_match_id:
        return DuplicateDto(
            is_duplicate=True,
            similar_complaint_id=best_match_id,
            similarity=round(best_similarity, 2)
        )

    return DuplicateDto(is_duplicate=False, similar_complaint_id=None, similarity=round(best_similarity, 2))

def run_ai_analysis(title: str, description: str, category: Optional[str] = None, priority: Optional[str] = None) -> AiDto:
    full_text = f"{title} {description}"
    cat, conf = classify_category(full_text)
    final_cat = category if (category and category != "GENERAL") else cat
    final_pri = priority if priority else predict_priority(full_text, final_cat)
    dept = DEPARTMENT_MAP.get(final_cat, "LPU General Estate Maintenance")
    sentiment = analyze_sentiment(full_text)
    
    summary = f"{final_cat.capitalize()} issue reported. Priority: {final_pri}. Assigned to {dept}."
    
    return AiDto(
        category=final_cat,
        priority=final_pri,
        department=dept,
        sentiment=sentiment,
        confidence=conf,
        summary=summary
    )
