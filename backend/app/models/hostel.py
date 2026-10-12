# backend/app/models/hostel.py
# Pydantic schemas for the Hostel document

from pydantic import BaseModel
from typing import Optional


class HostelResponse(BaseModel):
    id: str
    name: str
    block: str
    latitude: float
    longitude: float
    radiusMeters: int


class VerifyLocationRequest(BaseModel):
    hostelBlock: str
    latitude: float
    longitude: float
    accuracy: float          # GPS accuracy in meters reported by the device


class VerifyLocationResponse(BaseModel):
    verified: bool
    distanceMeters: float
    allowedRadius: int
    message: str


def hostel_doc_to_response(doc: dict) -> dict:
    doc["id"] = str(doc["_id"])
    return doc
