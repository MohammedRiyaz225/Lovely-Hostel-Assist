# backend/app/routers/hostels.py
# Endpoints: GET /api/hostels, POST /api/hostels/verify

from datetime import datetime
from fastapi import APIRouter, HTTPException, Depends
from typing import List

from config.database import get_db
from app.models.hostel import (
    HostelResponse, VerifyLocationRequest,
    VerifyLocationResponse, hostel_doc_to_response
)
from app.dependencies import get_current_user
from app.utils.haversine import haversine_meters

router = APIRouter(prefix="/api/hostels", tags=["Hostels"])

MAX_GPS_ACCURACY = 100.0  # metres — reject readings worse than this


@router.get("", response_model=List[HostelResponse])
async def list_hostels():
    """Returns all hostel blocks from the database."""
    db = get_db()
    cursor = db.hostels.find({})
    hostels = []
    async for doc in cursor:
        hostels.append(HostelResponse(**hostel_doc_to_response(doc)))
    return hostels


@router.post("/verify", response_model=VerifyLocationResponse)
async def verify_location(
    body: VerifyLocationRequest,
    current_user: dict = Depends(get_current_user)
):
    """
    Checks if the student is within the allowed radius of their selected hostel block.
    Rejects GPS readings with accuracy > 100 m.
    On success, marks the user as location-verified in MongoDB.
    """
    # 1. Reject poor GPS accuracy
    if body.accuracy > MAX_GPS_ACCURACY:
        raise HTTPException(
            status_code=400,
            detail=f"GPS accuracy is {body.accuracy:.0f} m, which is worse than the 100 m limit. "
                   "Move to an open area or wait for better signal."
        )

    # 2. Look up the hostel block
    db = get_db()
    hostel = await db.hostels.find_one({"block": body.hostelBlock})
    if not hostel:
        raise HTTPException(status_code=404, detail=f"Hostel block '{body.hostelBlock}' not found")

    # 3. Calculate Haversine distance
    distance = haversine_meters(
        body.latitude, body.longitude,
        hostel["latitude"], hostel["longitude"]
    )
    allowed_radius = hostel["radiusMeters"]
    verified = distance <= allowed_radius

    # 4. If verified, update the user document
    if verified:
        await db.users.update_one(
            {"_id": current_user["_id"]},
            {"$set": {
                "isLocationVerified": True,
                "verifiedAt": datetime.utcnow(),
                "hostelBlock": body.hostelBlock   # update in case block changed
            }}
        )
        message = f"✅ Verified! You are {distance:.0f} m from {hostel['name']} (allowed: {allowed_radius} m)"
    else:
        message = (f"❌ Not verified. You are {distance:.0f} m away, "
                   f"but must be within {allowed_radius} m of {hostel['name']}.")

    return VerifyLocationResponse(
        verified=verified,
        distanceMeters=round(distance, 1),
        allowedRadius=allowed_radius,
        message=message
    )
