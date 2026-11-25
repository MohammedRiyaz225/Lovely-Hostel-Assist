import time
from datetime import datetime, timedelta
from typing import Optional
from jose import jwt
from app.config import settings
from app.models import UserDto

# Accounts matching Android AuthRepository.kt predefined accounts
SAMPLE_ACCOUNTS = {
    # Student
    "student": {
        "id": "usr_student_101",
        "name": "Alex Rivera",
        "roll_number": "CS22B104",
        "email": "alex.rivera@hostel.lpu.in",
        "hostel_block": "BH-4",
        "room_number": "204",
        "role": "STUDENT",
        "password": "student123"
    },
    "cs22b104": {
        "id": "usr_student_101",
        "name": "Alex Rivera",
        "roll_number": "CS22B104",
        "email": "cs22b104@hostel.lpu.in",
        "hostel_block": "BH-4",
        "room_number": "204",
        "role": "STUDENT",
        "password": "student123"
    },
    "12201945": {
        "id": "usr_student_102",
        "name": "Harsh Vardhan",
        "roll_number": "12201945",
        "email": "12201945@hostel.lpu.in",
        "hostel_block": "BH-4",
        "room_number": "312",
        "role": "STUDENT",
        "password": "student123"
    },
    # Warden
    "warden": {
        "id": "usr_warden_201",
        "name": "Dr. Rajesh Sharma",
        "roll_number": "EMP-WD-401",
        "email": "warden.bh4@lpu.co.in",
        "hostel_block": "BH-4",
        "room_number": "Warden Office, Block 4",
        "role": "WARDEN",
        "password": "warden123"
    },
    "emp-wd-401": {
        "id": "usr_warden_201",
        "name": "Dr. Rajesh Sharma",
        "roll_number": "EMP-WD-401",
        "email": "warden.bh4@lpu.co.in",
        "hostel_block": "BH-4",
        "room_number": "Warden Office, Block 4",
        "role": "WARDEN",
        "password": "warden123"
    },
    # Staff
    "staff": {
        "id": "usr_staff_301",
        "name": "Ramesh Kumar",
        "roll_number": "EMP-MNT-782",
        "email": "ramesh.kumar@estate.lpu.in",
        "hostel_block": "Central Workshop Desk",
        "room_number": "Workshop Station A-1",
        "role": "STAFF",
        "password": "staff123"
    },
    "emp-mnt-782": {
        "id": "usr_staff_301",
        "name": "Ramesh Kumar",
        "roll_number": "EMP-MNT-782",
        "email": "ramesh.kumar@estate.lpu.in",
        "hostel_block": "Central Workshop Desk",
        "room_number": "Workshop Station A-1",
        "role": "STAFF",
        "password": "staff123"
    }
}

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)

def authenticate_user(email_or_id: str, password: str) -> Optional[UserDto]:
    clean_id = email_or_id.strip().lower()
    # Check predefined accounts
    if clean_id in SAMPLE_ACCOUNTS:
        acc = SAMPLE_ACCOUNTS[clean_id]
        if acc["password"] == password.strip():
            return UserDto(
                id=acc["id"],
                name=acc["name"],
                roll_number=acc["roll_number"],
                email=acc["email"],
                hostel_block=acc["hostel_block"],
                room_number=acc["room_number"],
                role=acc["role"]
            )

    # Dynamic self-service login
    if len(password.strip()) >= 3:
        if clean_id.startswith("emp-wd") or clean_id.startswith("wdn"):
            role = "WARDEN"
            name = f"Duty Warden ({clean_id.upper()})"
            block = "BH-4"
            room = "Warden Office"
        elif clean_id.startswith("emp-mnt") or clean_id.startswith("stf"):
            role = "STAFF"
            name = f"Technician ({clean_id.upper()})"
            block = "Central Workshop Desk"
            room = "Station A-1"
        else:
            role = "STUDENT"
            name = f"Student ({clean_id.upper()})"
            block = "BH-4"
            room = "204"

        return UserDto(
            id=f"usr_{abs(hash(clean_id)) % 100000}",
            name=name,
            roll_number=clean_id.upper(),
            email=f"{clean_id}@hostel.lpu.in",
            hostel_block=block,
            room_number=room,
            role=role
        )

    return None
