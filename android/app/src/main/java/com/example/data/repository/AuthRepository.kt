package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.api.LoginRequest
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    data class RegisteredAccount(
        val role: UserRole,
        val validIds: List<String>,
        val password: String,
        val userTemplate: (String) -> User
    )

    private val registeredAccounts = listOf(
        // Student Account - Registration No. only
        RegisteredAccount(
            role = UserRole.STUDENT,
            validIds = listOf("cs22b104", "12201945", "student"),
            password = "student123",
            userTemplate = { idInput ->
                User(
                    id = "usr_student_101",
                    name = "Alex Rivera",
                    rollNumber = idInput.uppercase(),
                    email = "${idInput.lowercase()}@hostel.lpu.in",
                    phone = "+91 98765 43210",
                    hostelBlock = "BH-4",
                    roomNumber = "204",
                    role = UserRole.STUDENT,
                    departmentOrTrade = "B.Tech CSE Resident (BH-4)",
                    assignedTrade = null,
                    token = "jwt_student_token"
                )
            }
        ),
        // Warden / Admin Account - Employee ID only
        RegisteredAccount(
            role = UserRole.WARDEN,
            validIds = listOf("emp-wd-401", "wdn-102", "warden", "admin"),
            password = "warden123",
            userTemplate = { idInput ->
                User(
                    id = "usr_warden_201",
                    name = "Dr. Rajesh Sharma",
                    rollNumber = idInput.uppercase(),
                    email = "${idInput.lowercase()}@hostel.lpu.in",
                    phone = "+91 1824 517204",
                    hostelBlock = "BH-4",
                    roomNumber = "Warden Office, Block 4",
                    role = UserRole.WARDEN,
                    departmentOrTrade = "Chief Warden - BH-4 & Campus Oversight",
                    assignedTrade = null,
                    token = "jwt_warden_token"
                )
            }
        ),
        // Maintenance Staff Account - Staff ID / Employee ID only
        RegisteredAccount(
            role = UserRole.STAFF,
            validIds = listOf("emp-mnt-782", "stf-503", "staff", "technician"),
            password = "staff123",
            userTemplate = { idInput ->
                User(
                    id = "usr_staff_301",
                    name = "Ramesh Kumar",
                    rollNumber = idInput.uppercase(),
                    email = "${idInput.lowercase()}@estate.lpu.in",
                    phone = "+91 98140 33211",
                    hostelBlock = "Central Workshop Desk",
                    roomNumber = "Workshop Station A-1",
                    role = UserRole.STAFF,
                    departmentOrTrade = "Senior Maintenance Staff (Plumbing & Electrical)",
                    assignedTrade = com.example.data.model.ComplaintCategory.PLUMBING,
                    token = "jwt_staff_token"
                )
            }
        )
    )

    suspend fun login(email: String, pass: String): Result<User> {
        val cleanId = email.trim().lowercase()
        val account = registeredAccounts.firstOrNull { acc ->
            acc.validIds.any { it.equals(cleanId, ignoreCase = true) } ||
            (acc.role == UserRole.STUDENT && (cleanId.startsWith("cs") || cleanId.all { it.isDigit() })) ||
            (acc.role == UserRole.WARDEN && (cleanId.startsWith("emp-wd") || cleanId.startsWith("wdn"))) ||
            (acc.role == UserRole.STAFF && (cleanId.startsWith("emp-mnt") || cleanId.startsWith("stf")))
        } ?: registeredAccounts.first { it.role == UserRole.STUDENT }

        return loginWithRole(email, pass, account.role)
    }

    suspend fun loginWithRole(email: String, pass: String, requestedRole: UserRole): Result<User> {
        val cleanId = email.trim()
        val cleanPass = pass.trim()

        if (cleanId.isBlank()) {
            val idLabel = when (requestedRole) {
                UserRole.STUDENT -> "Registration Number"
                UserRole.WARDEN -> "Employee ID"
                UserRole.STAFF -> "Staff ID / Employee ID"
            }
            return Result.failure(Exception("Please enter your $idLabel."))
        }
        if (cleanPass.isBlank()) {
            return Result.failure(Exception("Please enter your password (self-chosen password)."))
        }
        if (cleanPass.length < 3) {
            return Result.failure(Exception("Password must be at least 3 characters long."))
        }

        // Check if user matches a predefined registered account template
        val predefined = registeredAccounts.firstOrNull { acc ->
            acc.role == requestedRole && acc.validIds.any { it.equals(cleanId, ignoreCase = true) }
        }

        val user = if (predefined != null) {
            predefined.userTemplate(cleanId)
        } else {
            // Self-service login: dynamically generate college identity based on their entered Registration / Employee / Staff ID
            when (requestedRole) {
                UserRole.STUDENT -> {
                    val regNo = cleanId.uppercase()
                    User(
                        id = "usr_std_${regNo.hashCode().toString().takeLast(5)}",
                        name = "Student ($regNo)",
                        rollNumber = regNo,
                        email = "$regNo@hostel.lpu.in",
                        phone = "+91 98765 43210",
                        hostelBlock = "BH-4",
                        roomNumber = "204",
                        role = UserRole.STUDENT,
                        departmentOrTrade = "College Resident",
                        assignedTrade = null,
                        token = "jwt_student_${System.currentTimeMillis()}"
                    )
                }
                UserRole.WARDEN -> {
                    val empId = cleanId.uppercase()
                    User(
                        id = "usr_wd_${empId.hashCode().toString().takeLast(5)}",
                        name = "Duty Warden ($empId)",
                        rollNumber = empId,
                        email = "$empId@warden.lpu.in",
                        phone = "+91 1824 517204",
                        hostelBlock = "BH-4",
                        roomNumber = "Warden Office, Block 4",
                        role = UserRole.WARDEN,
                        departmentOrTrade = "Hostel Warden & Administration",
                        assignedTrade = null,
                        token = "jwt_warden_${System.currentTimeMillis()}"
                    )
                }
                UserRole.STAFF -> {
                    val staffId = cleanId.uppercase()
                    User(
                        id = "usr_stf_${staffId.hashCode().toString().takeLast(5)}",
                        name = "Technician ($staffId)",
                        rollNumber = staffId,
                        email = "$staffId@estate.lpu.in",
                        phone = "+91 98140 33211",
                        hostelBlock = "Central Workshop Desk",
                        roomNumber = "Workshop Station A-1",
                        role = UserRole.STAFF,
                        departmentOrTrade = "Campus Maintenance Staff",
                        assignedTrade = com.example.data.model.ComplaintCategory.PLUMBING,
                        token = "jwt_staff_${System.currentTimeMillis()}"
                    )
                }
            }
        }

        _currentUser.value = user
        return Result.success(user)
    }

    fun register(name: String, email: String, roll: String, block: String, room: String, role: UserRole): User {
        val user = User(
            id = "usr_${System.currentTimeMillis() % 1000}",
            name = name,
            rollNumber = roll,
            email = email,
            phone = "+91 98765 43210",
            hostelBlock = block,
            roomNumber = room,
            role = role,
            token = "mock_register_jwt"
        )
        _currentUser.value = user
        return user
    }

    fun switchRole(newRole: UserRole) {
        _currentUser.value = when (newRole) {
            UserRole.STUDENT -> User(
                id = "usr_student_101",
                name = "Alex Rivera",
                rollNumber = "CS22B104",
                email = "alex.rivera@hostel.lpu.in",
                phone = "+91 98765 43210",
                hostelBlock = "BH-4",
                roomNumber = "204",
                role = UserRole.STUDENT,
                departmentOrTrade = "B.Tech CSE Resident (BH-4)",
                assignedTrade = null,
                token = "jwt_student_demo_token"
            )
            UserRole.WARDEN -> User(
                id = "usr_warden_201",
                name = "Dr. Rajesh Sharma",
                rollNumber = "EMP-WD-401",
                email = "warden.bh4@lpu.co.in",
                phone = "+91 1824 517204",
                hostelBlock = "BH-4",
                roomNumber = "Warden Office, Block 4",
                role = UserRole.WARDEN,
                departmentOrTrade = "Chief Warden - BH-4 & Campus Oversight",
                assignedTrade = null,
                token = "jwt_warden_demo_token"
            )
            UserRole.STAFF -> User(
                id = "usr_staff_301",
                name = "Ramesh Kumar",
                rollNumber = "EMP-MNT-782",
                email = "ramesh.kumar@estate.lpu.in",
                phone = "+91 98140 33211",
                hostelBlock = "Central Workshop Desk",
                roomNumber = "Workshop Station A-1",
                role = UserRole.STAFF,
                departmentOrTrade = "Senior Maintenance Staff (Plumbing & Electrical)",
                assignedTrade = com.example.data.model.ComplaintCategory.PLUMBING,
                token = "jwt_staff_demo_token"
            )
        }
    }

    fun updateProfile(name: String, phone: String, block: String, room: String) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(
            name = name,
            phone = phone,
            hostelBlock = block,
            roomNumber = room
        )
    }

    fun logout() {
        _currentUser.value = null
    }
}
