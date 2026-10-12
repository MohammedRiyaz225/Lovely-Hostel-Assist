# HostelFix REST API Reference

Base URL: `http://localhost:8000` (or `http://10.0.2.2:8000` from Android Emulator)

All endpoints returning or accepting JSON use standard UTF-8 encoding.
Authenticated endpoints require header: `Authorization: Bearer <jwt_access_token>`.

---

## 1. Authentication Endpoints (`/api/auth`)

### `POST /api/auth/register`
Creates a new student account.

**Request Body (`application/json`):**
```json
{
  "name": "Rahul Verma",
  "email": "rahul.12100@lpu.in",
  "password": "Password@123",
  "phone": "9876543210",
  "hostelBlock": "BH-1",
  "roomNumber": "304-B"
}
```

**Response (`201 Created`):**
```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "bearer",
  "user": {
    "id": "65b98...",
    "name": "Rahul Verma",
    "email": "rahul.12100@lpu.in",
    "phone": "9876543210",
    "hostelBlock": "BH-1",
    "roomNumber": "304-B",
    "role": "STUDENT",
    "isLocationVerified": false,
    "verifiedAt": null,
    "createdAt": "2026-10-02T06:00:00Z"
  }
}
```

---

### `POST /api/auth/login`
Authenticates existing student or administrator.

**Request Body (`application/json`):**
```json
{
  "email": "rahul.12100@lpu.in",
  "password": "Password@123"
}
```

**Response (`200 OK`):**
```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "bearer",
  "user": { ... }
}
```

---

### `GET /api/auth/profile`
Returns current user's profile details. Requires Bearer Token.

---

## 2. Hostels & Geofencing (`/api/hostels`)

### `GET /api/hostels`
Returns a list of all hostel blocks registered in the database with their GPS coordinates and allowed radius.

**Response (`200 OK`):**
```json
[
  {
    "id": "65b9a...",
    "name": "Boys Hostel 1",
    "block": "BH-1",
    "latitude": 31.2464,
    "longitude": 75.7037,
    "radiusMeters": 150
  }
]
```

---

### `POST /api/hostels/verify`
Validates whether the student's mobile coordinates lie within the hostel's radius.

**Request Body (`application/json`):**
```json
{
  "hostelBlock": "BH-1",
  "latitude": 31.2465,
  "longitude": 75.7038,
  "accuracy": 8.5
}
```

**Response (`200 OK`):**
```json
{
  "verified": true,
  "distanceMeters": 14.2,
  "allowedRadius": 150,
  "message": "✅ Verified! You are 14 m from Boys Hostel 1 (allowed: 150 m)"
}
```

---

## 3. Complaints Management (`/api/complaints`)

### `POST /api/complaints`
Submits a new complaint with optional image evidence.

**Request Headers:** `Content-Type: multipart/form-data`

**Form Parts:**
- `title` (text): Short summary of the defect.
- `description` (text): Full description of the complaint.
- `hostelBlock` (text): Hostel identifier (e.g. `BH-1`).
- `roomNumber` (text): Room identifier (e.g. `304-B`).
- `submittedLat` (number, optional): GPS Latitude.
- `submittedLng` (number, optional): GPS Longitude.
- `image` (file, optional): JPEG/PNG/WEBP photo.

**Response (`201 Created`):**
```json
{
  "id": "65b9c...",
  "title": "Water leakage in washroom",
  "description": "The main pipe is leaking water continuously.",
  "hostelBlock": "BH-1",
  "roomNumber": "304-B",
  "status": "AI_ANALYZED",
  "imageUrl": "/uploads/a7b1...jpg",
  "submittedLat": 31.2465,
  "submittedLng": 75.7038,
  "locationVerified": true,
  "ai": {
    "category": "Plumbing",
    "priority": "High",
    "department": "Civil & Plumbing",
    "sentiment": "Negative",
    "sentimentScore": -0.45,
    "isDuplicate": false,
    "similarityScore": 0.12,
    "summary": "Continuous water leakage reported in washroom pipe."
  },
  "assignedTo": null,
  "adminComment": null,
  "resolutionComment": null,
  "feedback": null,
  "studentId": "65b98...",
  "studentName": "Rahul Verma",
  "createdAt": "2026-10-02T06:15:00Z",
  "updatedAt": "2026-10-02T06:15:02Z"
}
```

---

### `GET /api/complaints`
Retrieves all complaints submitted by the authenticated student (sorted newest first).

---

### `GET /api/complaints/{id}`
Returns full detail of a specific complaint.

---

### `GET /api/complaints/stats`
Returns complaint counts for the student dashboard.

**Response (`200 OK`):**
```json
{
  "total": 5,
  "pending": 2,
  "resolved": 2,
  "closed": 1
}
```

---

### `POST /api/complaints/{id}/feedback`
Allows students to submit rating and remarks once a complaint is resolved.

**Request Body (`application/json`):**
```json
{
  "rating": 5,
  "comment": "Repaired within 2 hours. Tap works perfectly!"
}
```

---

## 4. Notifications (`/api/notifications`)

### `GET /api/notifications`
Returns up to 30 latest notifications for the student.

### `PATCH /api/notifications/{id}/read`
Marks a notification as read.
