package com.example.data.model

data class User(
    val id: String = "usr_101",
    val name: String = "Alex Rivera",
    val rollNumber: String = "CS22B104",
    val email: String = "alex.rivera@hostel.edu",
    val phone: String = "+91 98765 43210",
    val hostelBlock: String = "BH-4",
    val roomNumber: String = "204",
    val role: UserRole = UserRole.STUDENT,
    val departmentOrTrade: String? = "B.Tech CSE Resident (BH-4)",
    val assignedTrade: ComplaintCategory? = null,
    val token: String = "mock_jwt_token_auth_xyz"
)

enum class UserRole(val displayName: String) {
    STUDENT("Student"),
    WARDEN("Hostel Warden"),
    STAFF("Maintenance Staff")
}

