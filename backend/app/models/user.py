# backend/app/models/user.py
# Pydantic schemas for the User document stored in MongoDB

from pydantic import BaseModel, EmailStr, Field
from typing import Optional
from datetime import datetime
from enum import Enum


class UserRole(str, Enum):
    STUDENT = "STUDENT"
    STAFF = "STAFF"
    ADMIN = "ADMIN"


# ---------- Request schemas ----------

class RegisterRequest(BaseModel):
    name: str = Field(..., min_length=2, max_length=100)
    email: EmailStr
    password: str = Field(..., min_length=6)
    phone: str = Field(..., min_length=10, max_length=15)
    hostelBlock: str
    roomNumber: str


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


# ---------- Response schemas ----------

class UserResponse(BaseModel):
    id: str
    name: str
    email: str
    phone: str
    hostelBlock: str
    roomNumber: str
    role: UserRole
    isLocationVerified: bool
    verifiedAt: Optional[datetime] = None
    createdAt: datetime


class LoginResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserResponse


# ---------- Internal document helpers ----------

def user_doc_to_response(doc: dict) -> dict:
    """Convert a raw MongoDB document to UserResponse-compatible dict."""
    doc["id"] = str(doc["_id"])
    return doc
