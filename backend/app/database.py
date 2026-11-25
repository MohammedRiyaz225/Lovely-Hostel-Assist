import sqlite3
import json
import os
import time
from typing import List, Optional, Dict, Any
from app.config import settings

class Database:
    def __init__(self, db_path: str = settings.SQLITE_DB_PATH):
        self.db_path = db_path
        self._init_db()
        self._seed_data_if_empty()

    def get_connection(self):
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        return conn

    def _init_db(self):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            # Complaints table
            cursor.execute("""
            CREATE TABLE IF NOT EXISTS complaints (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                block TEXT NOT NULL,
                room_number TEXT,
                student_name TEXT,
                student_roll TEXT,
                status TEXT NOT NULL DEFAULT 'PENDING',
                assigned_to TEXT,
                created_at TEXT,
                ai_json TEXT,
                duplicate_json TEXT,
                work_note TEXT
            );
            """)

            # Staff table
            cursor.execute("""
            CREATE TABLE IF NOT EXISTS staff (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                trade TEXT NOT NULL,
                phone TEXT NOT NULL,
                active_assignments INTEGER DEFAULT 0
            );
            """)
            conn.commit()

    def _seed_data_if_empty(self):
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT COUNT(*) FROM complaints;")
            count = cursor.fetchone()[0]
            if count == 0:
                self._seed_complaints(cursor)
                self._seed_staff(cursor)
                conn.commit()

    def _seed_staff(self, cursor):
        staff_members = [
            ("STF-PLUMB-101", "Ramesh Kumar", "PLUMBING", "+91 98140 33211", 2),
            ("STF-ELEC-102", "Baldev Singh", "ELECTRICAL", "+91 98140 44222", 1),
            ("STF-CLEAN-103", "Sunita Rani", "CLEANING", "+91 98140 55333", 0),
            ("STF-WIFI-104", "Vikram Patel", "WIFI", "+91 98140 66444", 1),
            ("STF-FURN-105", "Gurpreet Singh", "FURNITURE", "+91 98140 77555", 0),
        ]
        cursor.executemany("""
            INSERT OR IGNORE INTO staff (id, name, trade, phone, active_assignments)
            VALUES (?, ?, ?, ?, ?);
        """, staff_members)

    def _seed_complaints(self, cursor):
        initial_complaints = [
            (
                "CMP-2026",
                "Severe geyser water leakage in washroom",
                "The bathroom geyser pipe connector has burst and water is flooding into room 204. Need immediate plumbing support.",
                "BH-4",
                "204",
                "Alex Rivera",
                "CS22B104",
                "PENDING",
                None,
                "2026-11-25 09:15:00",
                json.dumps({
                    "category": "PLUMBING",
                    "priority": "HIGH",
                    "department": "LPU Estate Maintenance (Plumbing Division)",
                    "sentiment": "FRUSTRATED",
                    "confidence": 0.94,
                    "summary": "Geyser pipeline burst causing bathroom flooding in BH-4 204."
                }),
                json.dumps({"is_duplicate": False, "similar_complaint_id": None, "similarity": 0.0}),
                None
            ),
            (
                "CMP-2027",
                "Ceiling fan making loud grinding noise and sparking",
                "Regulator switch sparks when turned on. Fan is wobbling dangerously and making grinding sounds.",
                "BH-4",
                "312",
                "Harsh Vardhan",
                "12201945",
                "IN_PROGRESS",
                "Baldev Singh",
                "2026-11-24 14:30:00",
                json.dumps({
                    "category": "ELECTRICAL",
                    "priority": "CRITICAL",
                    "department": "LPU Electrical Power & Substations",
                    "sentiment": "NEGATIVE",
                    "confidence": 0.92,
                    "summary": "Electrical spark at regulator and unstable wobbly fan."
                }),
                json.dumps({"is_duplicate": False, "similar_complaint_id": None, "similarity": 0.0}),
                "Technician visited site, power isolated. Replacement capacitor ordered."
            ),
            (
                "CMP-2028",
                "Wing B 3rd Floor Wi-Fi access point down",
                "No internet connectivity across rooms 301 to 320 for past 12 hours. Captive portal not responding.",
                "BH-4",
                "Wing B",
                "Rohan Verma",
                "12204561",
                "PENDING",
                "Vikram Patel",
                "2026-11-25 08:00:00",
                json.dumps({
                    "category": "WIFI",
                    "priority": "HIGH",
                    "department": "LPU NetOps Campus IT Division",
                    "sentiment": "NEGATIVE",
                    "confidence": 0.89,
                    "summary": "Corridor Wi-Fi access point offline affecting entire 3rd floor wing."
                }),
                json.dumps({"is_duplicate": False, "similar_complaint_id": None, "similarity": 0.0}),
                None
            ),
            (
                "CMP-2029",
                "Study table drawer lock broken",
                "Drawer lock mechanism is jammed and key broke inside.",
                "BH-4",
                "108",
                "Priya Sharma",
                "12208892",
                "RESOLVED",
                "Gurpreet Singh",
                "2026-11-23 11:20:00",
                json.dumps({
                    "category": "FURNITURE",
                    "priority": "LOW",
                    "department": "LPU Civil & Carpentry Works",
                    "sentiment": "NEUTRAL",
                    "confidence": 0.85,
                    "summary": "Broken desk lock replaced with new master cylinder."
                }),
                json.dumps({"is_duplicate": False, "similar_complaint_id": None, "similarity": 0.0}),
                "Lock mechanism replaced with new cylinder key set."
            )
        ]
        cursor.executemany("""
            INSERT OR IGNORE INTO complaints (
                id, title, description, block, room_number, student_name, student_roll,
                status, assigned_to, created_at, ai_json, duplicate_json, work_note
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """, initial_complaints)

    def get_all_complaints(self) -> List[Dict[str, Any]]:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM complaints ORDER BY created_at DESC;")
            rows = cursor.fetchall()
            return [self._row_to_dict(r) for r in rows]

    def get_complaint_by_id(self, complaint_id: str) -> Optional[Dict[str, Any]]:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM complaints WHERE id = ?;", (complaint_id,))
            row = cursor.fetchone()
            return self._row_to_dict(row) if row else None

    def create_complaint(self, data: Dict[str, Any]) -> Dict[str, Any]:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO complaints (
                    id, title, description, block, room_number, student_name, student_roll,
                    status, assigned_to, created_at, ai_json, duplicate_json, work_note
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """, (
                data["id"],
                data["title"],
                data["description"],
                data["block"],
                data.get("room_number"),
                data.get("student_name", "Resident Student"),
                data.get("student_roll", "N/A"),
                data.get("status", "PENDING"),
                data.get("assigned_to"),
                data.get("created_at", time.strftime("%Y-%m-%d %H:%M:%S")),
                json.dumps(data.get("ai", {})),
                json.dumps(data.get("duplicate", {})),
                data.get("work_note")
            ))
            conn.commit()
            return self.get_complaint_by_id(data["id"])

    def update_complaint_status(self, complaint_id: str, status: str, assigned_to: Optional[str] = None, note: Optional[str] = None) -> Optional[Dict[str, Any]]:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            if assigned_to is not None and note is not None:
                cursor.execute("""
                    UPDATE complaints
                    SET status = ?, assigned_to = ?, work_note = ?
                    WHERE id = ?;
                """, (status, assigned_to, note, complaint_id))
            elif assigned_to is not None:
                cursor.execute("""
                    UPDATE complaints
                    SET status = ?, assigned_to = ?
                    WHERE id = ?;
                """, (status, assigned_to, complaint_id))
            elif note is not None:
                cursor.execute("""
                    UPDATE complaints
                    SET status = ?, work_note = ?
                    WHERE id = ?;
                """, (status, note, complaint_id))
            else:
                cursor.execute("""
                    UPDATE complaints
                    SET status = ?
                    WHERE id = ?;
                """, (status, complaint_id))
            conn.commit()
            return self.get_complaint_by_id(complaint_id)

    def get_all_staff(self) -> List[Dict[str, Any]]:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM staff ORDER BY name ASC;")
            rows = cursor.fetchall()
            return [dict(r) for r in rows]

    def _row_to_dict(self, row) -> Dict[str, Any]:
        d = dict(row)
        if d.get("ai_json"):
            try:
                d["ai"] = json.loads(d["ai_json"])
            except Exception:
                d["ai"] = None
        else:
            d["ai"] = None

        if d.get("duplicate_json"):
            try:
                d["duplicate"] = json.loads(d["duplicate_json"])
            except Exception:
                d["duplicate"] = None
        else:
            d["duplicate"] = None
        return d

db = Database()
