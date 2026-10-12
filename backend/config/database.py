# backend/config/database.py
# MongoDB Atlas connection using Motor (async driver)

import os
from motor.motor_asyncio import AsyncIOMotorClient
from dotenv import load_dotenv

load_dotenv()

MONGODB_URL = os.getenv("MONGODB_URL", "mongodb://localhost:27017/hostelfix")

client: AsyncIOMotorClient = None
db = None


async def connect_db():
    """Called on app startup – creates the Motor client and picks the database."""
    global client, db
    client = AsyncIOMotorClient(MONGODB_URL)
    db = client.get_default_database()          # uses the db name in the URI
    # Ensure indexes for common queries
    await db.users.create_index("email", unique=True)
    await db.complaints.create_index("studentId")
    await db.complaints.create_index([("hostelBlock", 1), ("status", 1)])
    await db.notifications.create_index("userId")
    print("MongoDB connected")


async def close_db():
    """Called on app shutdown – closes the Motor client."""
    global client
    if client:
        client.close()
        print("MongoDB connection closed")


def get_db():
    """Returns the live database handle; imported by routers."""
    return db
