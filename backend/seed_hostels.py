#!/usr/bin/env python
# backend/seed_hostels.py
# Run once to populate the hostels collection from hostels.csv
# Usage: python seed_hostels.py

import asyncio
import csv
from pathlib import Path
from motor.motor_asyncio import AsyncIOMotorClient
from dotenv import load_dotenv
import os

load_dotenv()

MONGODB_URL = os.getenv("MONGODB_URL", "mongodb://localhost:27017/hostelfix")
CSV_PATH = Path(__file__).parent / "hostels.csv"


async def seed():
    client = AsyncIOMotorClient(MONGODB_URL)
    db = client.get_default_database()

    hostels = []
    with open(CSV_PATH, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            # Skip comment lines (lines starting with #)
            if row["name"].startswith("#"):
                continue
            hostels.append({
                "name": row["name"].strip(),
                "block": row["block"].strip(),
                "latitude": float(row["latitude"]),
                "longitude": float(row["longitude"]),
                "radiusMeters": int(row["radiusMeters"]),
            })

    if not hostels:
        print("No hostel rows found in CSV. Add rows and re-run.")
        return

    # Replace all existing documents (idempotent re-seed)
    await db.hostels.delete_many({})
    result = await db.hostels.insert_many(hostels)
    print(f"Seeded {len(result.inserted_ids)} hostel blocks.")

    client.close()


if __name__ == "__main__":
    asyncio.run(seed())
