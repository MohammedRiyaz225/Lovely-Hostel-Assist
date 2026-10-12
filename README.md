# HostelFix (HostelSense)
### AI-Powered, Location-Verified Hostel Complaint Management System
**College Project for Lovely Professional University (LPU), Punjab**

---

## 🌟 Overview
**HostelFix** replaces paper registers and manual phone calls with a digital hostel complaint tracking system tailored for Lovely Professional University.

### Key Capabilities
- 📍 **GPS Geofence Verification**: Confirms student is physically inside their hostel block via Google Maps & FusedLocationProvider before prioritizing complaints.
- 🤖 **Native AI/NLP Engine**:
  - Automatically classifies category (Plumbing, Electrical, Carpentry, Wi-Fi, Housekeeping).
  - Estimates priority (`Low`, `Medium`, `High`, `Critical`).
  - Predicts responsible repair department.
  - Detects duplicate complaints using TF-IDF and Cosine Similarity.
  - Analyzes resident sentiment polarity.
- 📱 **Modern Android App**: 100% Kotlin with Jetpack Compose & Material 3, MVVM, Retrofit, Coroutines, DataStore, and Coil.
- 💻 **Admin Web Dashboard**: Interactive Jinja2 + HTML/CSS/JS portal for wardens to manage complaints, assign staff, and track resolution metrics.
- 🔔 **Push Notifications**: Firebase Cloud Messaging (FCM) dispatching alerts at each resolution step.
- ⭐ **Feedback Loop**: 5-star rating system with remarks once repairs are completed.

---

## 🏗️ Architecture

```
                  +-----------------------------------+
                  |      Android App (Student)        |
                  |  - Jetpack Compose (Material 3)   |
                  |  - Google Maps & GPS Geofence     |
                  |  - Retrofit HTTP + JWT Auth       |
                  |  - Firebase Cloud Messaging (FCM) |
                  +-----------------------------------+
                                    |
                            HTTP / JSON (REST)
                                    v
+--------------------------------------------------------------------------+
|                       FastAPI Backend Server                             |
|  - Asynchronous endpoints (Python 3.10+)                                 |
|  - JWT Authentication (bcrypt passwords)                                 |
|  - Haversine GPS Distance Calculation                                    |
|  - Admin Web Dashboard (Jinja2 Templates)                                |
|  - AI/NLP Subsystem:                                                     |
|      * TF-IDF & Logistic Regression Category Classifier                  |
|      * Cosine Similarity Duplicate Ingestion Filter                      |
|      * Priority & Department Recommendation Rules                        |
+--------------------------------------------------------------------------+
            |                                               |
            v                                               v
+-----------------------+                       +-----------------------+
|  MongoDB Atlas DB     |                       | Local Media Storage   |
|  - users              |                       | - uploads/ (Photos)   |
|  - hostels            |                       | - ai/models/*.joblib  |
|  - complaints         |                       +-----------------------+
|  - notifications      |
+-----------------------+
```

---

## 🚀 Quick Start Guide

### 1. Backend Setup & Run

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Create and activate a Python virtual environment:
   ```bash
   python -m venv .venv
   # Windows:
   .venv\Scripts\activate
   # Linux/Mac:
   source .venv/bin/activate
   ```

3. Install requirements:
   ```bash
   pip install -r requirements.txt
   ```

4. Configure environment:
   Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
   Set your MongoDB Atlas URI in `.env` (or use local MongoDB: `mongodb://localhost:27017`).

5. Seed Initial Data (Hostel blocks & maintenance staff):
   ```bash
   python seed_hostels.py
   python seed_staff.py
   ```

6. Train AI Classifier:
   ```bash
   python -m ai.train
   ```

7. Start the server:
   ```bash
   uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```
   - API Docs: `http://localhost:8000/docs`
   - Admin Panel: `http://localhost:8000/admin`
   - Health Check: `http://localhost:8000/health`

---

### 2. Android App Setup & Run

1. Open the `android` folder in **Android Studio** (Ladybug or newer recommended).
2. Ensure you have Android SDK 35 installed.
3. Configure `android/local.properties`:
   ```properties
   MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
   ```
4. Run on Android Emulator or physical device connected via USB:
   - For emulator: The app connects to the backend at `http://10.0.2.2:8000/`.
   - For physical device: Set your computer's local Wi-Fi IP address in `ApiClient.kt`.

---

## 📚 Project Documentation

Detailed design specifications can be found under `docs/`:
- [SRS.md](docs/SRS.md) – Software Requirements Specification
- [API.md](docs/API.md) – Full REST API Reference
- [DATABASE.md](docs/DATABASE.md) – MongoDB Schema & Collection Architecture
- [AI_MODULE.md](docs/AI_MODULE.md) – AI & NLP Subsystem Architecture
- [TEST_CHECKLIST.md](docs/TEST_CHECKLIST.md) – Comprehensive Testing and Viva Presentation Guide

---

## 👥 Contributors
Developed for Lovely Professional University (LPU) as a final-year college project.
