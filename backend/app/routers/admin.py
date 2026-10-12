# backend/app/routers/admin.py
# Admin-only endpoints: list complaints, assign staff, update status, analytics, AI insights

from datetime import datetime
from typing import List, Optional
from bson import ObjectId
from fastapi import APIRouter, Depends, HTTPException, Body

from config.database import get_db
from app.dependencies import require_role
from app.models.complaint import ComplaintResponse, complaint_doc_to_response

router = APIRouter(prefix="/api/admin", tags=["Admin"])

admin_guard = Depends(require_role("ADMIN"))
staff_guard = Depends(require_role("ADMIN", "STAFF"))


# ── List complaints with optional filters ─────────────────────────────────────

@router.get("/complaints", response_model=List[ComplaintResponse])
async def admin_list_complaints(
    status: Optional[str] = None,
    hostelBlock: Optional[str] = None,
    locationVerified: Optional[bool] = None,
    _admin=admin_guard
):
    db = get_db()
    query = {}
    if status:
        query["status"] = status
    if hostelBlock:
        query["hostelBlock"] = hostelBlock
    if locationVerified is not None:
        query["locationVerified"] = locationVerified

    cursor = db.complaints.find(query).sort("createdAt", -1)
    results = []
    async for doc in cursor:
        results.append(ComplaintResponse(**complaint_doc_to_response(doc)))
    return results


# ── Assign staff to a complaint ───────────────────────────────────────────────

@router.patch("/complaints/{complaint_id}/assign")
async def assign_complaint(
    complaint_id: str,
    staff_id: str = Body(..., embed=True),
    admin_comment: Optional[str] = Body(None, embed=True),
    _admin=admin_guard
):
    db = get_db()
    try:
        c_oid = ObjectId(complaint_id)
        s_oid = ObjectId(staff_id)
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid id")

    # Verify staff exists
    staff = await db.users.find_one({"_id": s_oid, "role": "STAFF"})
    if not staff:
        raise HTTPException(status_code=404, detail="Staff member not found")

    updated = await db.complaints.find_one_and_update(
        {"_id": c_oid},
        {"$set": {
            "assignedTo": s_oid,
            "status": "ASSIGNED",
            "adminComment": admin_comment,
            "updatedAt": datetime.utcnow()
        }},
        return_document=True
    )
    if not updated:
        raise HTTPException(status_code=404, detail="Complaint not found")

    # Create notification for student
    await db.notifications.insert_one({
        "userId": updated["studentId"],
        "title": "Complaint Assigned",
        "body": f"Your complaint '{updated['title']}' has been assigned to staff.",
        "complaintId": c_oid,
        "read": False,
        "createdAt": datetime.utcnow()
    })

    return complaint_doc_to_response(updated)


# ── Update complaint status ───────────────────────────────────────────────────

@router.patch("/complaints/{complaint_id}/status")
async def update_status(
    complaint_id: str,
    new_status: str = Body(..., embed=True),
    resolution_comment: Optional[str] = Body(None, embed=True),
    _user=staff_guard
):
    valid_statuses = ["SUBMITTED", "AI_ANALYZED", "ASSIGNED", "IN_PROGRESS",
                      "RESOLVED", "FEEDBACK", "CLOSED"]
    if new_status not in valid_statuses:
        raise HTTPException(status_code=400, detail=f"Invalid status. Choose from {valid_statuses}")

    db = get_db()
    try:
        oid = ObjectId(complaint_id)
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid id")

    update_fields = {"status": new_status, "updatedAt": datetime.utcnow()}
    if resolution_comment:
        update_fields["resolutionComment"] = resolution_comment

    updated = await db.complaints.find_one_and_update(
        {"_id": oid},
        {"$set": update_fields},
        return_document=True
    )
    if not updated:
        raise HTTPException(status_code=404, detail="Complaint not found")

    # Notify student of status change
    await db.notifications.insert_one({
        "userId": updated["studentId"],
        "title": f"Status Update: {new_status}",
        "body": f"Your complaint '{updated['title']}' is now {new_status}.",
        "complaintId": oid,
        "read": False,
        "createdAt": datetime.utcnow()
    })

    return complaint_doc_to_response(updated)


# ── Analytics ─────────────────────────────────────────────────────────────────

@router.get("/analytics")
async def get_analytics(_admin=admin_guard):
    db = get_db()

    total = await db.complaints.count_documents({})
    by_status = {}
    for s in ["SUBMITTED", "AI_ANALYZED", "ASSIGNED", "IN_PROGRESS",
              "RESOLVED", "FEEDBACK", "CLOSED"]:
        by_status[s] = await db.complaints.count_documents({"status": s})

    # Category breakdown
    pipeline_category = [
        {"$group": {"_id": "$ai.category", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}}
    ]
    cat_cursor = db.complaints.aggregate(pipeline_category)
    by_category = {}
    async for row in cat_cursor:
        key = row["_id"] or "Unknown"
        by_category[key] = row["count"]

    # Block breakdown
    pipeline_block = [
        {"$group": {"_id": "$hostelBlock", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}}
    ]
    blk_cursor = db.complaints.aggregate(pipeline_block)
    by_block = {}
    async for row in blk_cursor:
        key = row["_id"] or "Unknown"
        by_block[key] = row["count"]

    return {
        "total": total,
        "byStatus": by_status,
        "byCategory": by_category,
        "byBlock": by_block,
    }


# ── AI Insights ───────────────────────────────────────────────────────────────

@router.get("/insights")
async def get_ai_insights(_admin=admin_guard):
    db = get_db()

    duplicate_count = await db.complaints.count_documents({"ai.isDuplicate": True})

    # Most common category
    pipeline = [
        {"$match": {"ai.category": {"$ne": None}}},
        {"$group": {"_id": "$ai.category", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}},
        {"$limit": 5}
    ]
    top_categories = []
    async for row in db.complaints.aggregate(pipeline):
        top_categories.append({"category": row["_id"], "count": row["count"]})

    # Recurring issues by block
    pipeline2 = [
        {"$match": {"ai.category": {"$ne": None}}},
        {"$group": {
            "_id": {"block": "$hostelBlock", "category": "$ai.category"},
            "count": {"$sum": 1}
        }},
        {"$match": {"count": {"$gte": 2}}},
        {"$sort": {"count": -1}},
        {"$limit": 10}
    ]
    recurring = []
    async for row in db.complaints.aggregate(pipeline2):
        recurring.append({
            "block": row["_id"]["block"],
            "category": row["_id"]["category"],
            "count": row["count"]
        })

    return {
        "duplicateCount": duplicate_count,
        "topCategories": top_categories,
        "recurringIssuesByBlock": recurring
    }


# ── Staff list ────────────────────────────────────────────────────────────────

@router.get("/staff")
async def list_staff(_admin=admin_guard):
    db = get_db()
    cursor = db.users.find({"role": "STAFF"}, {"password": 0})
    staff = []
    async for doc in cursor:
        doc["id"] = str(doc.pop("_id"))
        staff.append(doc)
    return staff


# ── Notifications (admin view) ────────────────────────────────────────────────

@router.get("/notifications")
async def admin_notifications(_admin=admin_guard):
    db = get_db()
    cursor = db.notifications.find({}).sort("createdAt", -1).limit(50)
    result = []
    async for doc in cursor:
        doc["id"] = str(doc.pop("_id"))
        doc["userId"] = str(doc["userId"])
        doc["complaintId"] = str(doc["complaintId"])
        result.append(doc)
    return result
