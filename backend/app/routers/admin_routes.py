import os
from fastapi import APIRouter, Request, Form
from fastapi import status as http_status
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
from app.database import db

templates_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "templates")
templates = Jinja2Templates(directory=templates_dir)

router = APIRouter(tags=["Admin Web Portal"])

@router.get("/admin", response_class=HTMLResponse)
@router.get("/dashboard", response_class=HTMLResponse)
async def admin_dashboard(request: Request, block: str = "ALL", status_filter: str = "ALL"):
    all_complaints = db.get_all_complaints()
    staff_members = db.get_all_staff()

    # Compute statistics
    total = len(all_complaints)
    pending = sum(1 for c in all_complaints if c.get("status") == "PENDING")
    in_progress = sum(1 for c in all_complaints if c.get("status") == "IN_PROGRESS")
    resolved = sum(1 for c in all_complaints if c.get("status") == "RESOLVED")
    critical = sum(1 for c in all_complaints if c.get("ai", {}).get("priority") == "CRITICAL")

    # Filter complaints
    filtered = all_complaints
    if block != "ALL":
        filtered = [c for c in filtered if c.get("block") == block]
    if status_filter != "ALL":
        filtered = [c for c in filtered if c.get("status") == status_filter]

    # Unique blocks for dropdown
    blocks = sorted(list(set(c.get("block", "BH-4") for c in all_complaints)))

    return templates.TemplateResponse("dashboard.html", {
        "request": request,
        "complaints": filtered,
        "staff_members": staff_members,
        "stats": {
            "total": total,
            "pending": pending,
            "in_progress": in_progress,
            "resolved": resolved,
            "critical": critical
        },
        "selected_block": block,
        "selected_status": status_filter,
        "blocks": blocks
    })

@router.post("/admin/complaint/{id}/update")
async def update_complaint_from_admin(
    id: str,
    status: str = Form(...),
    assigned_to: str = Form(None),
    note: str = Form(None)
):
    assigned = assigned_to if (assigned_to and assigned_to != "None") else None
    db.update_complaint_status(complaint_id=id, status=status, assigned_to=assigned, note=note)
    return RedirectResponse(url="/admin", status_code=http_status.HTTP_303_SEE_OTHER)
