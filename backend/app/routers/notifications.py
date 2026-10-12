# backend/app/routers/notifications.py
# Endpoints: GET /api/notifications, PATCH /api/notifications/{id}/read

from fastapi import APIRouter, Depends
from bson import ObjectId
from config.database import get_db
from app.dependencies import get_current_user

router = APIRouter(prefix="/api/notifications", tags=["Notifications"])


@router.get("")
async def get_notifications(current_user: dict = Depends(get_current_user)):
    """Returns notifications for the current student, newest first."""
    db = get_db()
    cursor = db.notifications.find(
        {"userId": current_user["_id"]}
    ).sort("createdAt", -1).limit(30)

    results = []
    async for doc in cursor:
        doc["id"] = str(doc.pop("_id"))
        doc["userId"] = str(doc["userId"])
        if doc.get("complaintId"):
            doc["complaintId"] = str(doc["complaintId"])
        results.append(doc)
    return results


@router.patch("/{notification_id}/read")
async def mark_read(
    notification_id: str,
    current_user: dict = Depends(get_current_user)
):
    db = get_db()
    await db.notifications.update_one(
        {"_id": ObjectId(notification_id), "userId": current_user["_id"]},
        {"$set": {"read": True}}
    )
    return {"success": True}
