# backend/ai/complaint_service.py
# Orchestrates all AI modules and returns a merged result dict.
# Called by the complaints router immediately after saving the complaint.

from ai.classifier import predict_category
from ai.priority import predict_priority
from ai.department import get_department
from ai.location_extractor import extract_location
from ai.sentiment import analyze_sentiment
from ai.duplicate_detection import find_duplicate


async def analyze_complaint(complaint_doc: dict, db) -> dict:
    """
    Runs all AI analysis on a newly submitted complaint.

    Parameters:
        complaint_doc – the raw MongoDB document (already inserted).
        db – the live Motor database handle.

    Returns a dict that gets stored in complaint_doc["ai"].
    """
    text = f"{complaint_doc.get('title', '')} {complaint_doc.get('description', '')}"
    block = complaint_doc.get("hostelBlock", "")

    # 1. Category classification
    category = predict_category(text)

    # 2. Priority prediction
    priority = predict_priority(text, category)

    # 3. Department recommendation
    department = get_department(category)

    # 4. Location extraction from text
    extracted_location = extract_location(text)

    # 5. Sentiment analysis
    sentiment_result = analyze_sentiment(text)

    # 6. Duplicate detection (async – needs DB)
    dup_result = await find_duplicate(text, block, db)

    return {
        "category": category,
        "priority": priority,
        "department": department,
        "extractedLocation": extracted_location,
        "sentiment": sentiment_result.get("label"),
        "sentimentScore": sentiment_result.get("score"),
        "isDuplicate": dup_result.get("isDuplicate", False),
        "duplicateOf": dup_result.get("duplicateOf"),
        "similarityScore": dup_result.get("similarityScore", 0.0),
    }
