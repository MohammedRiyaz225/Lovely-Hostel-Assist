from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import RedirectResponse
from app.config import settings
from app.routers import auth_routes, complaint_routes, admin_routes

app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="Backend API and Warden Portal for HostelFix (Lovely Professional University Hostel Management System)"
)

# Enable CORS for Android clients, web emulators, and local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register API & Admin routers
app.include_router(auth_routes.router)
app.include_router(complaint_routes.router)
app.include_router(admin_routes.router)

@app.get("/")
async def root():
    # Redirect root to admin dashboard
    return RedirectResponse(url="/admin")

@app.get("/api/health")
async def health_check():
    return {
        "status": "healthy",
        "service": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "database": "SQLite (Active)",
        "ai_engine": "Rule-based & Heuristics (Active)"
    }
