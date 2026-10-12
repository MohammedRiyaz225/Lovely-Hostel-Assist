#!/usr/bin/env python
# backend/seed_staff.py
# Populates the database with admin and staff accounts.
# Usage: python seed_staff.py

import asyncio
from datetime import datetime
from motor.motor_asyncio import AsyncIOMotorClient
from passlib.context import CryptContext
from dotenv import load_dotenv
import os

load_dotenv()

MONGODB_URL = os.getenv("MONGODB_URL", "mongodb://localhost:27017/hostelfix")
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

STAFF_ACCOUNTS = [
    {
        "name": "Admin LPU",
        "email": "admin@hostelfix.lpu",
        "password": "Admin@123",
        "phone": "9999900000",
        "hostelBlock": "Admin",
        "roomNumber": "N/A",
        "role": "ADMIN",
    },
    {
        "name": "Ravi Kumar (Plumber)",
        "email": "ravi@hostelfix.lpu",
        "password": "Staff@123",
        "phone": "9999900001",
        "hostelBlock": "All",
        "roomNumber": "N/A",
        "role": "STAFF",
        "department": "Plumbing",
    },
    {
        "name": "Suresh Sharma (Electrician)",
        "email": "suresh@hostelfix.lpu",
        "password": "Staff@123",
        "phone": "9999900002",
        "hostelBlock": "All",
        "roomNumber": "N/A",
        "role": "STAFF",
        "department": "Electrical",
    },
    {
        "name": "Priya Devi (Cleaning)",
        "email": "priya@hostelfix.lpu",
        "password": "Staff@123",
        "phone": "9999900003",
        "hostelBlock": "All",
        "roomNumber": "N/A",
        "role": "STAFF",
        "department": "Cleaning",
    },
]


async def seed():
    client = AsyncIOMotorClient(MONGODB_URL)
    db = client.get_default_database()

    for acc in STAFF_ACCOUNTS:
        existing = await db.users.find_one({"email": acc["email"]})
        if existing:
            print(f"Already exists: {acc['email']}")
            continue

        acc_doc = {**acc, "password": pwd_context.hash(acc["password"])}
        acc_doc.update({
            "isLocationVerified": True,
            "verifiedAt": datetime.utcnow(),
            "createdAt": datetime.utcnow(),
        })
        await db.users.insert_one(acc_doc)
        print(f"Created: {acc['email']}  (role={acc['role']})")

    client.close()
    print("\nDone. Use these credentials to log in to the admin panel.")


if __name__ == "__main__":
    asyncio.run(seed())
