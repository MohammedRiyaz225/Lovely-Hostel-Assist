# backend/app/utils/haversine.py
# Pure-Python Haversine distance calculation (no extra dependencies)

import math


def haversine_meters(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """
    Returns the distance in metres between two GPS coordinates.
    Uses the Haversine formula (accurate enough for short distances).
    """
    R = 6_371_000  # Earth radius in metres

    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    d_phi = math.radians(lat2 - lat1)
    d_lam = math.radians(lon2 - lon1)

    a = (math.sin(d_phi / 2) ** 2
         + math.cos(phi1) * math.cos(phi2) * math.sin(d_lam / 2) ** 2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    return R * c
