# backend/app/routers/auth.py
# Endpoints: POST /api/auth/register, POST /api/auth/login, GET /api/auth/profile

import os
from datetime import datetime, timedelta
from fastapi import APIRouter, HTTPException, Depends, status
from passlib.context import CryptContext
from jose import jwt
from bson import ObjectId

from config.database import get_db
from app.models.user import (
    RegisterRequest, LoginRequest, LoginResponse,
    UserResponse, user_doc_to_response
)
from app.dependencies import get_current_user

router = APIRouter(prefix="/api/auth", tags=["Auth"])

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

JWT_SECRET_KEY    = os.getenv("JWT_SECRET_KEY", "default_secret_change_me")
JWT_ALGORITHM     = os.getenv("JWT_ALGORITHM", "HS256")
JWT_EXPIRE_MINUTES = int(os.getenv("JWT_EXPIRE_MINUTES", "10080"))  # 7 days


def _hash_password(plain: str) -> str:
    return pwd_context.hash(plain)


def _verify_password(plain: str, hashed: str) -> bool:
    return pwd_context.verify(plain, hashed)


def _create_token(user_id: str) -> str:
    expire = datetime.utcnow() + timedelta(minutes=JWT_EXPIRE_MINUTES)
    payload = {"sub": user_id, "exp": expire}
    return jwt.encode(payload, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)


@router.post("/register", response_model=LoginResponse, status_code=201)
async def register(body: RegisterRequest):
    db = get_db()

    # Check duplicate email
    existing = await db.users.find_one({"email": body.email.lower()})
    if existing:
        raise HTTPException(status_code=400, detail="Email already registered")

    user_doc = {
        "name": body.name,
        "email": body.email.lower(),
        "password": _hash_password(body.password),
        "phone": body.phone,
        "hostelBlock": body.hostelBlock,
        "roomNumber": body.roomNumber,
        "role": "STUDENT",
        "isLocationVerified": False,
        "verifiedAt": None,
        "createdAt": datetime.utcnow(),
    }
    result = await db.users.insert_one(user_doc)
    user_doc["_id"] = result.inserted_id

    token = _create_token(str(result.inserted_id))
    user_dict = user_doc_to_response(user_doc)

    return LoginResponse(
        access_token=token,
        user=UserResponse(**user_dict)
    )


@router.post("/login", response_model=LoginResponse)
async def login(body: LoginRequest):
    db = get_db()

    user = await db.users.find_one({"email": body.email.lower()})
    if not user or not _verify_password(body.password, user["password"]):
        raise HTTPException(status_code=401, detail="Invalid email or password")

    token = _create_token(str(user["_id"]))
    user_dict = user_doc_to_response(user)

    return LoginResponse(
        access_token=token,
        user=UserResponse(**user_dict)
    )


@router.get("/profile", response_model=UserResponse)
async def get_profile(current_user: dict = Depends(get_current_user)):
    user_dict = user_doc_to_response(current_user)
    return UserResponse(**user_dict)
