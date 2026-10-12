# HostelFix Project Test Checklist & Evaluation Guide

This checklist is structured for viva presentation, project demonstrations, and automated test execution.

---

## 1. Automated Test Suite (Backend)

Run from `HostelFix/backend`:
```bash
pytest tests/ -v
```

### Test Coverage Checklist
- [x] **`test_health`**: Verifies `/health` responds with HTTP 200 OK.
- [x] **`test_register_duplicate`**: Ensures registering the same email twice returns HTTP 400.
- [x] **`test_login_wrong_password`**: Verifies incorrect password returns HTTP 401 Unauthorized.
- [x] **`test_profile`**: Verifies authenticated student profile returns correct user metadata.
- [x] **`test_list_hostels`**: Ensures `/api/hostels` returns active campus hostel blocks.
- [x] **`test_verify_location_bad_accuracy`**: Rejects GPS coordinates with accuracy > 100 meters.
- [x] **`test_verify_location_outside`**: Returns `verified: false` when coordinates fall outside block perimeter.
- [x] **`test_submit_complaint`**: Tests complaint creation and confirms status transitions to `SUBMITTED` or `AI_ANALYZED`.
- [x] **`test_list_complaints`**: Confirms student can retrieve their own complaint list.
- [x] **`test_complaint_stats`**: Validates aggregate counts for total, pending, and resolved complaints.
- [x] **`test_admin_requires_auth`**: Confirms admin endpoints cannot be reached without credentials.

---

## 2. Interactive Feature Demonstration Checklist

### Scenario A: Student Registration & Login
1. Launch Android app -> Splash screen animates HostelFix logo.
2. If first launch, lands on Login Screen.
3. Tap "Register", enter name, email, password, phone, select hostel block (e.g. `BH-1`), and room number.
4. On success, auto-navigates to Home Screen with customized greeting.

### Scenario B: GPS Geofenced Verification
1. On Home Screen, note amber "Hostel GPS Not Verified" banner.
2. Tap "Verify Location".
3. Google Map displays with student's current location marker, selected hostel block marker, and perimeter circle.
4. Tap "Verify My Location".
5. If within radius -> Success banner appears, and Home Screen updates to green "Verified Resident" badge.

### Scenario C: Submitting a Complaint with AI Analysis
1. Tap "Submit New Complaint".
2. Fill title: *"Washroom tap broken and leaking water on 3rd floor"*.
3. Attach photo evidence using gallery picker.
4. Notice location badge verifies current coordinates.
5. Tap "Submit Complaint".
6. Detail screen opens:
   - Status: `AI_ANALYZED`
   - AI Insight Card shows:
     - **Category**: `Plumbing`
     - **Priority**: `High`
     - **Department**: `Civil & Plumbing`
     - **Duplicate**: `No`

### Scenario D: Duplicate Detection Demonstration
1. Submit a second complaint in the same block: *"Water leaking from broken tap in bathroom"*.
2. Open complaint details:
   - System flags duplicate warning banner: *"Matches existing complaint with 78% similarity"*.

### Scenario E: Admin Web Dashboard
1. Open browser at `http://localhost:8000/admin`.
2. Log in with admin credentials.
3. View analytics charts: Total complaints, open issues, category breakdown.
4. Filter complaints by Block and Priority.
5. Assign maintenance staff to open complaint.

### Scenario F: Resolution & Student Rating Feedback
1. Mark complaint as `RESOLVED` in admin panel.
2. Android student receives push notification.
3. Student opens complaint in app, views 5-star rating interface.
4. Submits 5-star rating and comment -> Status updates to `CLOSED`.
