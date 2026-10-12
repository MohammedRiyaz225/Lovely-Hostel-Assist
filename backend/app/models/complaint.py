# backend/app/models/complaint.py
# Pydantic schemas for the Complaint document

from pydantic import BaseModel, Field
from typing import Optional, Any, Dict
from datetime import datetime
from enum import Enum


class ComplaintStatus(str, Enum):
    SUBMITTED = "SUBMITTED"
    AI_ANALYZED = "AI_ANALYZED"
    ASSIGNED = "ASSIGNED"
    IN_PROGRESS = "IN_PROGRESS"
    RESOLVED = "RESOLVED"
    FEEDBACK = "FEEDBACK"
    CLOSED = "CLOSED"


class AIPrediction(BaseModel):
    category: Optional[str] = None
    priority: Optional[str] = None          # Low / Medium / High / Critical
    department: Optional[str] = None
    sentiment: Optional[str] = None         # Positive / Neutral / Negative
    sentimentScore: Optional[float] = None
    extractedLocation: Optional[str] = None
    isDuplicate: Optional[bool] = None
    duplicateOf: Optional[str] = None       # complaint id
    similarityScore: Optional[float] = None
    summary: Optional[str] = None


class FeedbackSchema(BaseModel):
    rating: int = Field(..., ge=1, le=5)
    comment: Optional[str] = None


# ---------- Response ----------

class ComplaintResponse(BaseModel):
    id: str
    title: str
    description: str
    hostelBlock: str
    roomNumber: str
    status: ComplaintStatus
    imageUrl: Optional[str] = None
    submittedLat: Optional[float] = None
    submittedLng: Optional[float] = None
    locationVerified: bool
    ai: Optional[Dict[str, Any]] = None
    assignedTo: Optional[str] = None
    adminComment: Optional[str] = None
    resolutionComment: Optional[str] = None
    feedback: Optional[Dict[str, Any]] = None
    studentId: str
    studentName: Optional[str] = None
    createdAt: datetime
    updatedAt: datetime


class ComplaintStats(BaseModel):
    total: int
    pending: int
    resolved: int
    closed: int


def complaint_doc_to_response(doc: dict) -> dict:
    """Convert raw MongoDB document to ComplaintResponse-compatible dict."""
    doc["id"] = str(doc["_id"])
    if doc.get("studentId"):
        doc["studentId"] = str(doc["studentId"])
    if doc.get("assignedTo"):
        doc["assignedTo"] = str(doc["assignedTo"])
    return doc
