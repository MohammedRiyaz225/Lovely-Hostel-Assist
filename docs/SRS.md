# Software Requirements Specification (SRS)
## HostelFix (HostelSense) – Smart Hostel Complaint Management System
### Lovely Professional University (LPU), Punjab

---

## 1. Introduction

### 1.1 Purpose
This document specifies the software requirements for **HostelFix** (also branded as **HostelSense**), an AI-powered and location-verified hostel complaint management system designed specifically for the residential campus of Lovely Professional University (LPU).

### 1.2 Scope
HostelFix replaces outdated, physical paper registers and uncoordinated messaging channels with an intelligent, end-to-end digital complaint tracking ecosystem comprising:
1. **Student Android App**: Kotlin, Jetpack Compose, Material 3, Google Maps GPS verification, real-time status tracking, and AI feedback.
2. **FastAPI Backend Server**: Async RESTful APIs using Python, Motor (MongoDB Atlas), and JWT authentication.
3. **AI/NLP Engine**: Automated category classification, priority prediction, duplicate complaint detection, department routing, sentiment scoring, and summarization.
4. **Administrative Web Portal**: Jinja2 + HTML5/CSS3/JavaScript dashboard for wardens and maintenance supervisors.
5. **Push Notifications**: Firebase Cloud Messaging (FCM) integration for real-time dispatch alerts.

---

## 2. Overall Description

### 2.1 Product Perspective
In large university campuses like LPU housing over 25,000+ hostel residents across numerous Boys' Hostels (BH-1 to BH-8) and Girls' Hostels (GH-1 to GH-6), maintenance complaints frequently suffer from duplicate logging, misrouted departments, lack of urgency differentiation, and untraceable turnaround times. HostelFix provides an automated pipeline from initial GPS-validated logging to verified resolution and student feedback.

```
+--------------------------------------------------------------+
|                     Android Client (Student)                 |
|  - Jetpack Compose UI           - Google Maps / GPS Geofence |
|  - Retrofit HTTP Client         - Firebase Push Receiver     |
+--------------------------------------------------------------+
                               |  ^
                      HTTP/JWT |  | Push (FCM)
                               v  |
+--------------------------------------------------------------+
|                      FastAPI Backend                         |
|  - JWT Auth & Security          - GPS Haversine Verifier     |
|  - Complaint CRUD & Uploads     - Jinja2 Admin Portal        |
+--------------------------------------------------------------+
            |                                    ^
            v                                    |
+--------------------------+          +------------------------+
|      MongoDB Atlas       |          |      AI/NLP Engine     |
| - users      - hostels   |          | - TF-IDF Classifier    |
| - complaints - notifs    |          | - Cosine Sim (Dups)    |
+--------------------------+          | - Sentiment & Priority |
                                      +------------------------+
```

### 2.2 User Classes and Roles
1. **Student Resident**: Registers with campus email, verifies hostel block via GPS geofencing, submits complaints with photo evidence, tracks resolution progress, and provides feedback ratings.
2. **Hostel Warden / Administrator**: Views aggregate analytics, filters complaints by block/department/priority, inspects AI recommendations, assigns maintenance staff, and closes complaints.
3. **Maintenance Staff**: Receives job assignments for plumbing, electrical, carpentry, or IT/Wi-Fi repairs.

---

## 3. Functional Requirements

### 3.1 Authentication & Profile
- **FR-1.1**: Students shall register using their name, LPU email, phone number, designated hostel block, and room number.
- **FR-1.2**: Passwords must be securely hashed using bcrypt prior to database insertion.
- **FR-1.3**: Authenticated sessions shall be maintained via JSON Web Tokens (JWT) signed with HMAC-SHA256.

### 3.2 GPS-Based Hostel Location Verification
- **FR-2.1**: The system shall fetch the student's exact coordinates using `FusedLocationProviderClient`.
- **FR-2.2**: GPS coordinates with accuracy worse than 100 meters shall be rejected with guidance to move to an open area.
- **FR-2.3**: The server shall compute the Haversine distance between the submitted coordinates and the hostel block's geographic center.
- **FR-2.4**: If the student is within the designated block radius (typically 100m–150m), their profile and submission shall be stamped as `locationVerified = True`.

### 3.3 Complaint Submission & Image Upload
- **FR-3.1**: Students can submit complaints with title, description, room number, hostel block, GPS coordinates, and an optional image evidence file (.jpg, .png, .webp).
- **FR-3.2**: Image uploads are saved on the server and referenced by relative URI.

### 3.4 AI Analysis & Duplicate Detection
- **FR-4.1**: Every complaint is analyzed upon ingestion:
  - **Category Classification**: Machine learning model categorizes into Plumbing, Electrical, Carpentry, Wi-Fi/Internet, Cleanliness, etc.
  - **Priority Scoring**: Predicts priority levels: `Low`, `Medium`, `High`, or `Critical` based on urgency keywords.
  - **Department Assignment**: Automates routing to Electrical, Civil, IT Infrastructure, or Housekeeping.
  - **Duplicate Detection**: Computes TF-IDF cosine similarity against open complaints in the same block. Flags potential duplicates (similarity > 0.65).
  - **Sentiment & Summary**: Evaluates sentiment polarity and extracts key summary phrases.

### 3.5 Lifecycle Tracking & Feedback
- **FR-5.1**: Complaints transition through explicit states: `SUBMITTED` -> `AI_ANALYZED` -> `ASSIGNED` -> `IN_PROGRESS` -> `RESOLVED` -> `CLOSED`.
- **FR-5.2**: Upon reaching `RESOLVED`, students can rate the fix (1 to 5 stars) with optional remarks.

---

## 4. Non-Functional Requirements
- **NFR-1 (Performance)**: AI inference and duplicate matching must complete in under 200 ms per complaint.
- **NFR-2 (Security)**: Passwords encrypted with bcrypt; API routes protected by role-based JWT; file uploads sanitized.
- **NFR-3 (Reliability)**: Failure in AI analysis or push notification delivery shall never abort complaint storage.
- **NFR-4 (Usability)**: Material 3 UI design compliant with modern Android accessibility guidelines.
