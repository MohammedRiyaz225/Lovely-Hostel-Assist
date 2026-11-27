import sys
import os
import asyncio
import httpx

# Add backend directory to sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from main import app

async def run_all_tests():
    transport = httpx.ASGITransport(app=app)
    async with httpx.AsyncClient(transport=transport, base_url="http://testserver") as client:
        print("Testing GET /api/health...")
        res = await client.get("/api/health")
        print(f" -> Status: {res.status_code}, Body: {res.json()}")
        assert res.status_code == 200

        print("\nTesting POST /api/auth/login...")
        res = await client.post("/api/auth/login", json={"email": "cs22b104", "password": "student123"})
        print(f" -> Status: {res.status_code}, User: {res.json().get('user')}")
        assert res.status_code == 200
        assert "token" in res.json()

        print("\nTesting GET /api/complaints...")
        res = await client.get("/api/complaints")
        complaints = res.json()
        print(f" -> Status: {res.status_code}, Loaded: {len(complaints)} complaints")
        assert res.status_code == 200
        assert len(complaints) >= 4

        print("\nTesting POST /api/complaints (Create new ticket with AI categorization)...")
        payload = {
            "title": "Water cooler in BH-4 1st floor not chilling",
            "description": "The water cooler is dispensing warm water since yesterday morning.",
            "block": "BH-4",
            "room_number": "1st Floor Lobby",
            "category": "PLUMBING",
            "priority": "MEDIUM"
        }
        res = await client.post("/api/complaints", json=payload)
        data = res.json()
        print(f" -> Status: {res.status_code}, Created ID: {data.get('id')}, Category: {data.get('ai', {}).get('category')}, Dept: {data.get('ai', {}).get('department')}")
        assert res.status_code == 200
        assert data["status"] == "PENDING"
        cid = data["id"]

        print(f"\nTesting PUT /api/complaints/{cid}/status (Assign tech & update status)...")
        update_payload = {
            "status": "IN_PROGRESS",
            "assigned_to": "Ramesh Kumar",
            "note": "Replaced thermostat sensor and refilled refrigerant gas."
        }
        res = await client.put(f"/api/complaints/{cid}/status", json=update_payload)
        updated_data = res.json()
        print(f" -> Status: {res.status_code}, Status: {updated_data.get('status')}, AssignedTo: {updated_data.get('assigned_to')}")
        assert res.status_code == 200
        assert updated_data["status"] == "IN_PROGRESS"
        assert updated_data["assigned_to"] == "Ramesh Kumar"

        print("\nTesting GET /admin (Jinja2 Warden Web Portal)...")
        res = await client.get("/admin")
        print(f" -> Status: {res.status_code}, HTML length: {len(res.text)} bytes")
        assert res.status_code == 200
        assert "HostelFix" in res.text

        print(f"\nTesting POST /admin/complaint/{cid}/update (Admin form update)...")
        res = await client.post(
            f"/admin/complaint/{cid}/update",
            data={"status": "RESOLVED", "assigned_to": "Ramesh Kumar", "note": "Verified from admin"},
            follow_redirects=False
        )
        print(f" -> Status: {res.status_code}")
        assert res.status_code == 303

    print("\n==========================================")
    print("🎉 ALL API & DASHBOARD TESTS PASSED! ✅")
    print("==========================================")

if __name__ == "__main__":
    asyncio.run(run_all_tests())
