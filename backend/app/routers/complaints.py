# backend/app/routers/complaints.py
# Endpoints: POST /api/complaints, GET /api/complaints, GET /api/complaints/{id},
#            GET /api/complaints/stats, POST /api/complaints/{id}/feedback

import os
import shutil
import uuid
from datetime import datetime
from pathlib import Path
from typing import List, Optional

from bson import ObjectId
from fastapi import (APIRouter, Depends, File, Form,
                     HTTPException, UploadFile, status)

from config.database import get_db
from app.dependencies import get_current_user, require_role
from app.models.complaint import (ComplaintResponse, ComplaintStats,
                                   FeedbackSchema, complaint_doc_to_response)
from app.utils.haversine import haversine_meters

router = APIRouter(prefix="/api/complaints", tags=["Complaints"])

UPLOAD_DIR = Path("uploads")
UPLOAD_DIR.mkdir(exist_ok=True)

PENDING_STATUSES = ["SUBMITTED", "AI_ANALYZED", "ASSIGNED", "IN_PROGRESS", "FEEDBACK"]

# ── helper ──────────────────────────────────────────────────────────────────

async def _check_location_verified(db, block: str, lat: Optional[float], lng: Optional[float]) -> bool:
    """Returns True if the given coordinate falls inside the hostel block's radius."""
    if lat is None or lng is None:
        return False
    hostel = await db.hostels.find_one({"block": block})
    if not hostel:
        return False
    distance = haversine_meters(lat, lng, hostel["latitude"], hostel["longitude"])
    return distance <= hostel["radiusMeters"]


# ── POST /api/complaints ─────────────────────────────────────────────────────

@router.post("", response_model=ComplaintResponse, status_code=201)
async def submit_complaint(
    title: str = Form(...),
    description: str = Form(...),
    hostelBlock: str = Form(...),
    roomNumber: str = Form(...),
    submittedLat: Optional[float] = Form(None),
    submittedLng: Optional[float] = Form(None),
    image: Optional[UploadFile] = File(None),
    current_user: dict = Depends(get_current_user),
):
    db = get_db()

    # Save image if uploaded
    image_url = None
    if image and image.filename:
        ext = Path(image.filename).suffix.lower()
        if ext not in {".jpg", ".jpeg", ".png", ".webp"}:
            raise HTTPException(status_code=400, detail="Only JPG/PNG/WEBP images allowed")
        filename = f"{uuid.uuid4()}{ext}"
        dest = UPLOAD_DIR / filename
        with dest.open("wb") as f:
            shutil.copyfileobj(image.file, f)
        image_url = f"/uploads/{filename}"

    # Check if submission coordinate is inside hostel radius
    location_verified = await _check_location_verified(
        db, hostelBlock, submittedLat, submittedLng
    )

    now = datetime.utcnow()
    complaint_doc = {
        "title": title,
        "description": description,
        "hostelBlock": hostelBlock,
        "roomNumber": roomNumber,
        "imageUrl": image_url,
        "submittedLat": submittedLat,
        "submittedLng": submittedLng,
        "locationVerified": location_verified,
        "status": "SUBMITTED",
        "ai": {},          # filled later by AI service
        "assignedTo": None,
        "adminComment": None,
        "resolutionComment": None,
        "feedback": None,
        "studentId": current_user["_id"],
        "studentName": current_user.get("name"),
        "createdAt": now,
        "updatedAt": now,
    }

    result = await db.complaints.insert_one(complaint_doc)
    complaint_doc["_id"] = result.inserted_id

    # Run AI analysis asynchronously (import here to avoid circular import)
    try:
        from ai.complaint_service import analyze_complaint
        ai_result = await analyze_complaint(complaint_doc, db)
        await db.complaints.update_one(
            {"_id": result.inserted_id},
            {"$set": {
                "ai": ai_result,
                "status": "AI_ANALYZED",
                "updatedAt": datetime.utcnow()
            }}
        )
        complaint_doc["ai"] = ai_result
        complaint_doc["status"] = "AI_ANALYZED"
    except Exception as e:
        # AI failure must NOT block complaint submission
        print(f"[AI] Analysis failed: {e}")

    return ComplaintResponse(**complaint_doc_to_response(complaint_doc))


# ── GET /api/complaints/stats ─────────────────────────────────────────────────

@router.get("/stats", response_model=ComplaintStats)
async def get_stats(current_user: dict = Depends(get_current_user)):
    db = get_db()
    student_id = current_user["_id"]

    total = await db.complaints.count_documents({"studentId": student_id})
    pending = await db.complaints.count_documents(
        {"studentId": student_id, "status": {"$in": PENDING_STATUSES}}
    )
    resolved = await db.complaints.count_documents(
        {"studentId": student_id, "status": "RESOLVED"}
    )
    closed = await db.complaints.count_documents(
        {"studentId": student_id, "status": "CLOSED"}
    )

    return ComplaintStats(total=total, pending=pending, resolved=resolved, closed=closed)


# ── GET /api/complaints ────────────────────────────────────────────────────────

@router.get("", response_model=List[ComplaintResponse])
async def list_complaints(current_user: dict = Depends(get_current_user)):
    """Returns all complaints submitted by the logged-in student, newest first."""
    db = get_db()
    cursor = db.complaints.find(
        {"studentId": current_user["_id"]}
    ).sort("createdAt", -1)

    complaints = []
    async for doc in cursor:
        complaints.append(ComplaintResponse(**complaint_doc_to_response(doc)))
    return complaints


# ── GET /api/complaints/{id} ───────────────────────────────────────────────────

@router.get("/{complaint_id}", response_model=ComplaintResponse)
async def get_complaint(
    complaint_id: str,
    current_user: dict = Depends(get_current_user)
):
    db = get_db()
    try:
        oid = ObjectId(complaint_id)
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid complaint id")

    doc = await db.complaints.find_one({"_id": oid})
    if not doc:
        raise HTTPException(status_code=404, detail="Complaint not found")

    # Students may only view their own complaints; admins/staff see all
    role = current_user.get("role")
    if role == "STUDENT" and doc["studentId"] != current_user["_id"]:
        raise HTTPException(status_code=403, detail="Access denied")

    return ComplaintResponse(**complaint_doc_to_response(doc))


# ── POST /api/complaints/{id}/feedback ────────────────────────────────────────

@router.post("/{complaint_id}/feedback", response_model=ComplaintResponse)
async def submit_feedback(
    complaint_id: str,
    body: FeedbackSchema,
    current_user: dict = Depends(get_current_user)
):
    db = get_db()
    try:
        oid = ObjectId(complaint_id)
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid complaint id")

    doc = await db.complaints.find_one({"_id": oid})
    if not doc:
        raise HTTPException(status_code=404, detail="Complaint not found")
    if doc["studentId"] != current_user["_id"]:
        raise HTTPException(status_code=403, detail="Access denied")
    if doc["status"] not in ("RESOLVED", "FEEDBACK"):
        raise HTTPException(status_code=400, detail="Complaint must be RESOLVED before feedback")

    feedback = {"rating": body.rating, "comment": body.comment, "submittedAt": datetime.utcnow()}
    updated = await db.complaints.find_one_and_update(
        {"_id": oid},
        {"$set": {"feedback": feedback, "status": "CLOSED", "updatedAt": datetime.utcnow()}},
        return_document=True
    )
    return ComplaintResponse(**complaint_doc_to_response(updated))
