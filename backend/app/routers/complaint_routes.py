import time
import random
from typing import List, Optional
from fastapi import APIRouter, HTTPException, status
from app.models import (
    ComplaintRequest,
    ComplaintResponse,
    StatusUpdateRequest,
    StaffDto,
    AiDto,
    DuplicateDto
)
from app.database import db
from app.ai_engine import run_ai_analysis, detect_duplicate

router = APIRouter(prefix="/api", tags=["Complaints & Staff"])

@router.get("/complaints", response_model=List[ComplaintResponse])
async def get_complaints():
    complaints = db.get_all_complaints()
    result = []
    for c in complaints:
        result.append(ComplaintResponse(
            id=c["id"],
            title=c["title"],
            description=c["description"],
            block=c["block"],
            room_number=c.get("room_number"),
            student_name=c.get("student_name"),
            student_roll=c.get("student_roll"),
            status=c.get("status", "PENDING"),
            assigned_to=c.get("assigned_to"),
            created_at=c.get("created_at"),
            ai=AiDto(**c["ai"]) if c.get("ai") else None,
            duplicate=DuplicateDto(**c["duplicate"]) if c.get("duplicate") else None
        ))
    return result

@router.get("/complaints/{id}", response_model=ComplaintResponse)
async def get_complaint(id: str):
    c = db.get_complaint_by_id(id)
    if not c:
        raise HTTPException(status_code=404, detail="Complaint not found")
    return ComplaintResponse(
        id=c["id"],
        title=c["title"],
        description=c["description"],
        block=c["block"],
        room_number=c.get("room_number"),
        student_name=c.get("student_name"),
        student_roll=c.get("student_roll"),
        status=c.get("status", "PENDING"),
        assigned_to=c.get("assigned_to"),
        created_at=c.get("created_at"),
        ai=AiDto(**c["ai"]) if c.get("ai") else None,
        duplicate=DuplicateDto(**c["duplicate"]) if c.get("duplicate") else None
    )

@router.post("/complaints", response_model=ComplaintResponse)
async def create_complaint(complaint: ComplaintRequest):
    # Retrieve existing complaints for duplicate detection
    existing = db.get_all_complaints()
    dup_info = detect_duplicate(complaint.title, complaint.description, complaint.block, existing)

    # Run AI pipeline (classification, priority, department, sentiment, summary)
    ai_info = run_ai_analysis(
        title=complaint.title,
        description=complaint.description,
        category=complaint.category,
        priority=complaint.priority
    )

    # Generate unique complaint ID
    new_id = f"CMP-{random.randint(2100, 9999)}"
    created_timestamp = time.strftime("%Y-%m-%d %H:%M:%S")

    data = {
        "id": new_id,
        "title": complaint.title,
        "description": complaint.description,
        "block": complaint.block,
        "room_number": complaint.room_number,
        "student_name": "Resident Student",
        "student_roll": "CS22B104",
        "status": "PENDING",
        "assigned_to": None,
        "created_at": created_timestamp,
        "ai": ai_info.model_dump(),
        "duplicate": dup_info.model_dump(),
        "work_note": None
    }

    saved = db.create_complaint(data)
    return ComplaintResponse(
        id=saved["id"],
        title=saved["title"],
        description=saved["description"],
        block=saved["block"],
        room_number=saved.get("room_number"),
        student_name=saved.get("student_name"),
        student_roll=saved.get("student_roll"),
        status=saved.get("status", "PENDING"),
        assigned_to=saved.get("assigned_to"),
        created_at=saved.get("created_at"),
        ai=AiDto(**saved["ai"]) if saved.get("ai") else None,
        duplicate=DuplicateDto(**saved["duplicate"]) if saved.get("duplicate") else None
    )

@router.put("/complaints/{id}/status", response_model=ComplaintResponse)
async def update_complaint_status(id: str, request: StatusUpdateRequest):
    updated = db.update_complaint_status(
        complaint_id=id,
        status=request.status,
        assigned_to=request.assigned_to,
        note=request.note
    )
    if not updated:
        raise HTTPException(status_code=404, detail="Complaint not found")
    return ComplaintResponse(
        id=updated["id"],
        title=updated["title"],
        description=updated["description"],
        block=updated["block"],
        room_number=updated.get("room_number"),
        student_name=updated.get("student_name"),
        student_roll=updated.get("student_roll"),
        status=updated.get("status"),
        assigned_to=updated.get("assigned_to"),
        created_at=updated.get("created_at"),
        ai=AiDto(**updated["ai"]) if updated.get("ai") else None,
        duplicate=DuplicateDto(**updated["duplicate"]) if updated.get("duplicate") else None
    )

@router.get("/staff", response_model=List[StaffDto])
async def get_staff_members():
    staff_list = db.get_all_staff()
    return [StaffDto(**s) for s in staff_list]
