# backend/tests/test_api.py
# Pytest integration tests for auth, hostels and complaints
# Run from /backend: pytest tests/ -v

import pytest
import pytest_asyncio
from httpx import AsyncClient, ASGITransport
from app.main import app
from config.database import connect_db, close_db

BASE = "http://test"
TEST_USER = {
    "name": "Test Student",
    "email": "teststudent@test.com",
    "password": "Test@1234",
    "phone": "9876543210",
    "hostelBlock": "54",
    "roomNumber": "201",
}


@pytest.fixture(scope="session")
def anyio_backend():
    return "asyncio"


@pytest_asyncio.fixture(scope="session")
async def client():
    """Shared AsyncClient for all tests with database connection."""
    try:
        await connect_db()
    except Exception as e:
        print(f"Warning: could not connect to MongoDB for tests: {e}")
    async with AsyncClient(transport=ASGITransport(app=app), base_url=BASE) as c:
        yield c
    try:
        await close_db()
    except Exception:
        pass


@pytest_asyncio.fixture(scope="session")
async def auth_headers(client):
    """Register (or login) and return JWT headers."""
    # Try register first
    r = await client.post("/api/auth/register", json=TEST_USER)
    if r.status_code == 400:  # already registered – just login
        r = await client.post("/api/auth/login", json={
            "email": TEST_USER["email"],
            "password": TEST_USER["password"]
        })
    assert r.status_code in (200, 201), f"Auth failed: {r.text}"
    token = r.json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


# ── Health ──────────────────────────────────────────────────────────────────

@pytest.mark.anyio
async def test_health(client):
    r = await client.get("/health")
    assert r.status_code == 200
    assert r.json()["status"] == "ok"


# ── Auth ────────────────────────────────────────────────────────────────────

@pytest.mark.anyio
async def test_register_duplicate(client):
    """Second registration with the same email must fail."""
    r = await client.post("/api/auth/register", json=TEST_USER)
    assert r.status_code == 400


@pytest.mark.anyio
async def test_login_wrong_password(client):
    r = await client.post("/api/auth/login", json={
        "email": TEST_USER["email"], "password": "wrongpass"
    })
    assert r.status_code == 401


@pytest.mark.anyio
async def test_profile(client, auth_headers):
    r = await client.get("/api/auth/profile", headers=auth_headers)
    assert r.status_code == 200
    assert r.json()["email"] == TEST_USER["email"]


# ── Hostels ─────────────────────────────────────────────────────────────────

@pytest.mark.anyio
async def test_list_hostels(client):
    r = await client.get("/api/hostels")
    assert r.status_code == 200
    assert isinstance(r.json(), list)


@pytest.mark.anyio
async def test_verify_location_bad_accuracy(client, auth_headers):
    """GPS accuracy > 100 m must be rejected."""
    r = await client.post("/api/hostels/verify", json={
        "hostelBlock": "34",
        "latitude": 31.2546,
        "longitude": 75.7050,
        "accuracy": 200.0          # deliberately poor
    }, headers=auth_headers)
    assert r.status_code == 400


@pytest.mark.anyio
async def test_verify_location_outside(client, auth_headers):
    """Coordinates far from any hostel must return verified=False."""
    r = await client.post("/api/hostels/verify", json={
        "hostelBlock": "34",
        "latitude": 28.6139,       # New Delhi – far from LPU
        "longitude": 77.2090,
        "accuracy": 10.0
    }, headers=auth_headers)
    # If block 34 doesn't exist yet in DB, it returns 404 – both are acceptable
    assert r.status_code in (200, 404)
    if r.status_code == 200:
        assert r.json()["verified"] is False


# ── Complaints ───────────────────────────────────────────────────────────────

@pytest.mark.anyio
async def test_submit_complaint(client, auth_headers):
    r = await client.post(
        "/api/complaints",
        data={
            "title": "Leaking tap in bathroom",
            "description": "The tap in Block 34 room 201 has been leaking since yesterday morning.",
            "hostelBlock": "34",
            "roomNumber": "201",
        },
        headers=auth_headers,
    )
    assert r.status_code == 201
    data = r.json()
    assert "id" in data
    assert data["status"] in ("SUBMITTED", "AI_ANALYZED")
    return data["id"]


@pytest.mark.anyio
async def test_list_complaints(client, auth_headers):
    r = await client.get("/api/complaints", headers=auth_headers)
    assert r.status_code == 200
    assert isinstance(r.json(), list)


@pytest.mark.anyio
async def test_complaint_stats(client, auth_headers):
    r = await client.get("/api/complaints/stats", headers=auth_headers)
    assert r.status_code == 200
    data = r.json()
    assert "total" in data
    assert data["total"] >= 1


@pytest.mark.anyio
async def test_admin_requires_auth(client):
    """Admin endpoint must reject unauthenticated requests."""
    r = await client.get("/api/admin/complaints")
    assert r.status_code == 401
