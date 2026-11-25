from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime

class LoginRequest(BaseModel):
    email: str
    password: str

class UserDto(BaseModel):
    id: str
    name: str
    roll_number: Optional[str] = None
    email: str
    hostel_block: Optional[str] = None
    room_number: Optional[str] = None
    role: str  # STUDENT, WARDEN, STAFF

class LoginResponse(BaseModel):
    token: str
    user: UserDto
    message: str = "Login successful"

class ComplaintRequest(BaseModel):
    title: str
    description: str
    block: str
    room_number: str
    category: Optional[str] = None
    priority: Optional[str] = None
    image_url: Optional[str] = None

class AiDto(BaseModel):
    category: Optional[str] = None
    priority: Optional[str] = None
    department: Optional[str] = None
    sentiment: Optional[str] = None
    confidence: Optional[float] = None
    summary: Optional[str] = None

class DuplicateDto(BaseModel):
    is_duplicate: bool = False
    similar_complaint_id: Optional[str] = None
    similarity: Optional[float] = 0.0

class ComplaintResponse(BaseModel):
    id: str
    title: str
    description: str
    block: str
    room_number: Optional[str] = None
    student_name: Optional[str] = None
    student_roll: Optional[str] = None
    status: str = "PENDING"  # PENDING, IN_PROGRESS, RESOLVED, REJECTED
    assigned_to: Optional[str] = None
    created_at: Optional[str] = None
    ai: Optional[AiDto] = None
    duplicate: Optional[DuplicateDto] = None

class StatusUpdateRequest(BaseModel):
    status: str
    assigned_to: Optional[str] = None
    note: Optional[str] = None

class StaffDto(BaseModel):
    id: str
    name: str
    trade: str
    phone: str
    active_assignments: int = 0
