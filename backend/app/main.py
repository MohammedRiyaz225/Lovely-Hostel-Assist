# backend/app/main.py
# FastAPI application entry-point

import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from pathlib import Path
from dotenv import load_dotenv

load_dotenv()

from config.database import connect_db, close_db
from app.routers import auth, hostels, complaints, admin, notifications

# ── Admin panel (Jinja2) ─────────────────────────────────────────────────────
TEMPLATES_DIR = Path(__file__).parent.parent / "templates"
templates = Jinja2Templates(directory=str(TEMPLATES_DIR))

# ── App factory ───────────────────────────────────────────────────────────────
app = FastAPI(
    title="HostelFix API",
    description="Hostel Complaint Management System – LPU",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

# ── CORS (allow Android emulator + local dev) ─────────────────────────────────
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],          # tighten to your domain in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Static files (uploaded images) ───────────────────────────────────────────
uploads_dir = Path("uploads")
uploads_dir.mkdir(exist_ok=True)
app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")

# ── Startup / shutdown ────────────────────────────────────────────────────────
@app.on_event("startup")
async def startup():
    await connect_db()


@app.on_event("shutdown")
async def shutdown():
    await close_db()


# ── API Routers ───────────────────────────────────────────────────────────────
app.include_router(auth.router)
app.include_router(hostels.router)
app.include_router(complaints.router)
app.include_router(admin.router)
app.include_router(notifications.router)


# ── Health check ──────────────────────────────────────────────────────────────
@app.get("/health", tags=["Health"])
async def health():
    return {"status": "ok", "service": "HostelFix API"}


# ── Admin panel routes (Jinja2 HTML) ─────────────────────────────────────────
from fastapi import Request
from fastapi.responses import HTMLResponse

@app.get("/admin", response_class=HTMLResponse, include_in_schema=False)
async def admin_dashboard(request: Request):
    return templates.TemplateResponse("admin/dashboard.html", {"request": request})

@app.get("/admin/complaints", response_class=HTMLResponse, include_in_schema=False)
async def admin_complaints_page(request: Request):
    return templates.TemplateResponse("admin/complaints.html", {"request": request})

@app.get("/admin/insights", response_class=HTMLResponse, include_in_schema=False)
async def admin_insights_page(request: Request):
    return templates.TemplateResponse("admin/insights.html", {"request": request})

@app.get("/admin/login", response_class=HTMLResponse, include_in_schema=False)
async def admin_login_page(request: Request):
    return templates.TemplateResponse("admin/login.html", {"request": request})

@app.get("/privacy-policy", response_class=HTMLResponse, tags=["Legal"])
async def privacy_policy_page(request: Request):
    """Google Play Store compliance privacy policy endpoint."""
    return templates.TemplateResponse("privacy_policy.html", {"request": request})
