# HostelFix - Python FastAPI Backend & Warden Portal

FastAPI backend server for the **HostelFix** Android application (Lovely Professional University Hostel & Maintenance Management System).

## Features
- **Android Retrofit API Matching**: Implements all 5 endpoints defined in `ApiService.kt`:
  - `POST /api/auth/login` (JWT authentication for Student, Warden, and Staff)
  - `POST /api/complaints` (Create new complaints with on-the-fly AI enrichment)
  - `GET /api/complaints` (List all complaints)
  - `GET /api/complaints/{id}` (Get specific complaint details)
  - `PUT /api/complaints/{id}/status` (Update complaint status, assign staff, add work notes)
  - `GET /api/staff` (List maintenance staff directory)
- **Built-in AI Analysis Engine**:
  - Automated category classification (Plumbing, Electrical, Cleaning, Wi-Fi, Food, Furniture, Security, General)
  - Priority prediction (Critical, High, Medium, Low)
  - Department routing (e.g. LPU Estate Maintenance, Electrical Substations, NetOps IT)
  - Sentiment analysis (Positive, Neutral, Negative, Frustrated)
  - Jaccard similarity-based duplicate complaint detection within the same hostel block
- **Jinja2 Warden Admin Portal**:
  - Accessible in web browser at `http://localhost:8000/admin`
  - Real-time ticket statistics (Total, Pending, In Repair, Resolved, Critical)
  - Filter by Block (BH-1 to BH-8, GH-1 to GH-6) and Status
  - Interactive modal to triage tickets, allocate technicians, and append work notes
- **Zero-Config Database**:
  - Built-in SQLite database (`hostelfix.db`) with pre-seeded realistic campus complaints
  - Ready for MongoDB integration by setting `MONGODB_URI` in `.env`
- **Interactive Swagger Documentation**:
  - OpenAPI Docs available at `http://localhost:8000/docs`

---

## Quick Start

### Option 1: Double-click `start.bat`
Simply double-click `start.bat` in the `backend/` directory.

### Option 2: Run via Command Line
```powershell
cd backend
python run.py
```

---

## Connecting the Android App

1. **Android Emulator**:
   - The app's default base URL is `http://10.0.2.2:8000/`.
   - As soon as the server is running, the Android app will automatically sync complaints.

2. **Physical Android Device**:
   - Make sure your computer and phone are connected to the same Wi-Fi.
   - Find your computer's local IP (e.g., `192.168.1.15`).
   - Open the HostelFix Android app -> Go to **Profile** -> Scroll to **Python FastAPI Backend Endpoint**.
   - Enter `http://192.168.1.15:8000/` and tap **Save & Sync**.

---

## Pre-Configured Test Accounts

| Role | Registration / Employee ID | Password | Access Level |
|---|---|---|---|
| **Student** | `cs22b104` or `12201945` | `student123` | File issues, track room maintenance, chat with AI |
| **Warden** | `emp-wd-401` or `warden` | `warden123` | Triage complaints, assign staff, review escalations |
| **Technician** | `emp-mnt-782` or `staff` | `staff123` | View assigned work orders, update status |
