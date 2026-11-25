# HostelFix (Lovely Hostel Assist)
### AI-Powered Hostel Complaint & Maintenance Management System
**College Project for Lovely Professional University (LPU), Punjab**

---

## 🌟 Overview
**HostelFix** replaces paper registers and manual phone calls with an AI-powered hostel complaint & maintenance management system tailored for **Lovely Professional University (LPU)**.

### Key Capabilities
- 🤖 **On-Device & Cloud AI Engine**:
  - **Automated Category Classification**: Plumbing, Electrical, Cleaning, Wi-Fi, Food, Furniture, Security, General.
  - **Priority Prediction**: Critical, High, Medium, Low.
  - **Department Routing**: Automatically assigns tickets to the right LPU division (e.g., *LPU Estate Maintenance*, *Electrical Substations*, *NetOps Campus IT*).
  - **Duplicate Detection**: Detects similar active complaints in the same hostel block using token similarity.
  - **Sentiment Analysis**: Detects urgency and frustration levels.
- 💬 **Groq AI Campus Assistant**:
  - Built-in conversational AI assistant powered by Groq (`Llama 3.3 70B` / `Llama 3.1 8B`) with specialized knowledge of all 18+ LPU hostel blocks (`BH-1`..`BH-8`, `GH-1`..`GH-6`, `Uni-Living`, `Block 34/36/38/55/56`), emergency helplines, and troubleshooting steps, plus offline fallback guidance.
- 📱 **Modern Offline-First Android App**:
  - Built in **100% Kotlin + Jetpack Compose + Material 3**.
  - **Room (SQLite)** local database for instant offline support + **Retrofit** asynchronous sync with the FastAPI backend.
  - Role-based access for **Students**, **Wardens**, and **Maintenance Staff**.
- 💻 **Python FastAPI Backend & Warden Web Portal**:
  - FastAPI REST API matching the Android Retrofit contract (`/api/auth/login`, `/api/complaints`, `/api/complaints/{id}/status`, `/api/staff`).
  - Interactive **Jinja2 Warden Web Dashboard** (`/admin`) to triage tickets, filter by block/status, assign technicians, and add work notes.
  - Zero-config SQLite persistence (`hostelfix.db`) with pre-seeded campus tickets.

---

## 🏗️ Project Structure

```
Lovely-Hostel-Assist/
├── android/               # Native Kotlin + Jetpack Compose Android App
│   ├── app/               # Source code, Room DB, Groq client, UI screens
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── backend/               # Python FastAPI Server & Jinja2 Warden Portal
│   ├── app/
│   │   ├── ai_engine.py   # Server-side classification, priority & duplicate detection
│   │   ├── database.py    # SQLite persistence & seed data
│   │   ├── models.py      # Pydantic schemas
│   │   ├── routers/       # Auth, Complaints & Admin Web Portal routes
│   │   └── templates/     # Jinja2 HTML dashboard
│   ├── main.py
│   ├── run.py
│   ├── start.bat
│   └── test_api.py
├── docs/                  # SRS, API Reference, Database & AI Documentation
└── README.md
```

---

## 🚀 Quick Start

### 1. Run the Backend Server
```powershell
cd backend
python run.py
```
Or double-click `backend/start.bat` on Windows.

Once running:
* **Warden Web Portal**: `http://localhost:8000/admin`
* **Swagger API Docs**: `http://localhost:8000/docs`
* **Health Check**: `http://localhost:8000/api/health`

### 2. Run the Android App
1. Open the `android/` folder in **Android Studio**.
2. Create `.env` from `android/.env.example` and add your `GROQ_API_KEY` (optional — offline fallback works without a key).
3. Run on an **Android Emulator** (`http://10.0.2.2:8000/` connects automatically to your local backend) or a **Physical Device** (set your PC's Wi-Fi IP in the app's Profile screen under *Python FastAPI Backend Endpoint*).

---

## 📚 Documentation
Detailed specifications can be found in [`docs/`](docs/):
- [SRS.md](docs/SRS.md) – Software Requirements Specification
- [API.md](docs/API.md) – REST API Reference
- [DATABASE.md](docs/DATABASE.md) – Database Schema
- [AI_MODULE.md](docs/AI_MODULE.md) – AI & NLP Subsystem Architecture
- [TEST_CHECKLIST.md](docs/TEST_CHECKLIST.md) – Testing Guide
