from fastapi import APIRouter, HTTPException, status
from app.models import LoginRequest, LoginResponse
from app.auth import authenticate_user, create_access_token

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

@router.post("/login", response_model=LoginResponse)
async def login(request: LoginRequest):
    user = authenticate_user(request.email, request.password)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid Registration Number / Employee ID or password"
        )
    token = create_access_token({"sub": user.id, "role": user.role, "email": user.email})
    return LoginResponse(
        token=token,
        user=user,
        message="Login successful"
    )
