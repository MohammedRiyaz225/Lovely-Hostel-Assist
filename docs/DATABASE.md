# Database Architecture & Schema Specification

**Database Engine**: MongoDB Atlas / MongoDB Community Server  
**Driver**: Motor (Async Python Driver)  
**Database Name**: `hostelfix`

---

## 1. Entity Relationship Overview

```
 [ users ] 1 ───────────< N [ complaints ] 1 ─────── 0..1 [ feedback ]
     │                           │
     │                           ├─── references [ hostels ] (hostelBlock)
     v                           v
 [ notifications ]           [ staff ] (assignedTo)
```

---

## 2. Collections Schema

### 2.1 `users` Collection
Stores student residents and administrative personnel.

| Field | Type | Description | Index |
|---|---|---|---|
| `_id` | ObjectId | Primary Key | Default |
| `name` | String | Full user name | - |
| `email` | String | LPU student email | Unique |
| `password` | String | Bcrypt password hash | - |
| `phone` | String | 10-digit mobile number | - |
| `hostelBlock` | String | Hostel block code (e.g. `BH-1`) | Indexed |
| `roomNumber` | String | Room code (e.g. `204-B`) | - |
| `role` | String | `STUDENT`, `STAFF`, `ADMIN` | Indexed |
| `isLocationVerified`| Boolean | GPS geofence validation state | - |
| `verifiedAt` | ISODate | Timestamp of last GPS verification| - |
| `fcmToken` | String | Firebase Cloud Messaging device push token | - |
| `createdAt` | ISODate | Registration timestamp | - |

---

### 2.2 `hostels` Collection
Master lookup for campus hostel blocks with geocoordinates and allowed radius.

| Field | Type | Description |
|---|---|---|
| `_id` | ObjectId | Primary Key |
| `name` | String | Friendly display name (e.g. `Boys Hostel 1`) |
| `block` | String | Unique block identifier (e.g. `BH-1`) [Unique Index] |
| `latitude` | Double | WGS84 GPS Latitude |
| `longitude`| Double | WGS84 GPS Longitude |
| `radiusMeters`| Integer | Allowed perimeter radius in meters (e.g. 150) |

---

### 2.3 `complaints` Collection
Core document tracking issues, lifecycle, AI predictions, and resolution.

| Field | Type | Description |
|---|---|---|
| `_id` | ObjectId | Primary Key |
| `title` | String | Short title |
| `description` | String | Defect description |
| `hostelBlock` | String | Block identifier |
| `roomNumber` | String | Room identifier |
| `imageUrl` | String (null)| Relative upload path (`/uploads/...`) |
| `submittedLat` | Double (null)| Latitude at time of logging |
| `submittedLng` | Double (null)| Longitude at time of logging |
| `locationVerified`| Boolean | True if coordinates were inside hostel radius |
| `status` | String | `SUBMITTED`, `AI_ANALYZED`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `FEEDBACK`, `CLOSED` |
| `ai` | Object | Subdocument for AI predictions |
| `ai.category` | String | Predicted category (Plumbing, Electrical, etc.) |
| `ai.priority` | String | `Low`, `Medium`, `High`, `Critical` |
| `ai.department` | String | Recommended maintenance department |
| `ai.sentiment` | String | `Positive`, `Neutral`, `Negative` |
| `ai.isDuplicate` | Boolean | True if similarity with open issue > 0.65 |
| `ai.similarityScore`| Double | Highest cosine similarity found |
| `ai.summary` | String | Extractive 1-line problem summary |
| `assignedTo` | String (null)| Name/ID of maintenance worker |
| `adminComment` | String (null)| Warden/supervisor note |
| `resolutionComment`| String (null)| Staff note on completion |
| `feedback` | Object (null)| Student rating (1-5) and feedback comment |
| `studentId` | ObjectId | Reference to `users._id` [Indexed] |
| `studentName` | String | Student name snapshot |
| `createdAt` | ISODate | Creation timestamp [Indexed, -1] |
| `updatedAt` | ISODate | Last update timestamp |

---

### 2.4 `notifications` Collection
Stores student-facing push and in-app alert records.

| Field | Type | Description |
|---|---|---|
| `_id` | ObjectId | Primary Key |
| `userId` | ObjectId | Reference to student |
| `complaintId`| ObjectId | Associated complaint |
| `message` | String | Notification text |
| `read` | Boolean | Read receipt flag |
| `createdAt` | ISODate | Timestamp |
