import os
from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    APP_NAME: str = "HostelFix Backend API"
    APP_VERSION: str = "1.0.0"
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    DEBUG: bool = True

    # Database: uses /tmp on Vercel (read-only app dir), local SQLite otherwise
    SQLITE_DB_PATH: str = (
        "/tmp/hostelfix.db"
        if os.getenv("VERCEL")
        else os.path.join(os.path.dirname(os.path.dirname(__file__)), "hostelfix.db")
    )
    MONGODB_URI: Optional[str] = os.getenv("MONGODB_URI", None)
    MONGODB_DB_NAME: str = "hostelfix"

    # JWT Authentication
    SECRET_KEY: str = os.getenv("SECRET_KEY", "hostelfix-lpu-secret-key-2026-secure")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # 7 days

    class Config:
        env_file = ".env"
        extra = "allow"

settings = Settings()
