# backend/ai/department.py
# Maps a complaint category to the responsible department/team

DEPARTMENT_MAP = {
    "Plumbing":     "Civil & Plumbing Department",
    "Electrical":   "Electrical Maintenance Department",
    "Cleaning":     "Housekeeping & Sanitation Department",
    "Food":         "Mess & Catering Department",
    "Internet/WiFi":"IT & Network Department",
    "Furniture":    "Civil & Maintenance Department",
    "Security":     "Security Department",
    "Maintenance":  "Civil & Maintenance Department",
    "Other":        "Hostel Warden Office",
}


def get_department(category: str) -> str:
    """Returns the responsible department for a given category."""
    return DEPARTMENT_MAP.get(category, "Hostel Warden Office")
