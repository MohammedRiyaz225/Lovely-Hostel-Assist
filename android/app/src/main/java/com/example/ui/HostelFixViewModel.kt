package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.HostelFixAIEngine
import com.example.data.api.ApiClient
import com.example.data.local.AppDatabase
import com.example.data.model.AIAnalysis
import com.example.data.model.AppNotification
import com.example.data.model.Complaint
import com.example.data.model.ComplaintCategory
import com.example.data.model.ComplaintPriority
import com.example.data.model.ComplaintStatus
import com.example.data.model.DuplicateInfo
import com.example.data.model.NotificationType
import com.example.data.model.SentimentType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.ComplaintRepository
import com.example.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ComplaintFilterState(
    val statusTab: String = "ALL", // ALL, PENDING, IN_PROGRESS, RESOLVED, UNASSIGNED, CRITICAL
    val scopeTab: String = "ALL", // ALL, MY_ROOM, BLOCK_FEED, ASSIGNED_ME, UNASSIGNED
    val selectedCategory: ComplaintCategory? = null,
    val selectedPriority: ComplaintPriority? = null,
    val selectedBlock: String? = null,
    val searchQuery: String = ""
)

data class ComplaintStats(
    val total: Int = 0,
    val pending: Int = 0,
    val inProgress: Int = 0,
    val resolved: Int = 0,
    val highPriority: Int = 0,
    val unassignedCount: Int = 0,
    val myAssignedCount: Int = 0
)

data class LiveAiState(
    val category: ComplaintCategory = ComplaintCategory.GENERAL,
    val priority: ComplaintPriority = ComplaintPriority.MEDIUM,
    val department: String = "General Estate Maintenance",
    val sentiment: SentimentType = SentimentType.NEUTRAL,
    val confidence: Float = 0.75f,
    val duplicateInfo: DuplicateInfo = DuplicateInfo(isDuplicate = false),
    val isAnalyzing: Boolean = false
)

class HostelFixViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val complaintRepository = ComplaintRepository(db.complaintDao())
    val authRepository = AuthRepository()
    val notificationRepository = NotificationRepository()

    val currentUser: StateFlow<User?> = authRepository.currentUser
    val notifications: StateFlow<List<AppNotification>> = notificationRepository.notifications

    private val _filterState = MutableStateFlow(ComplaintFilterState())
    val filterState: StateFlow<ComplaintFilterState> = _filterState.asStateFlow()

    private val _liveAiState = MutableStateFlow(LiveAiState())
    val liveAiState: StateFlow<LiveAiState> = _liveAiState.asStateFlow()

    private val _backendUrl = MutableStateFlow(ApiClient.getBaseUrl())
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val notificationCounter = java.util.concurrent.atomic.AtomicInteger(100)

    private fun nextNotificationId(prefix: String = "NOTIF"): String {
        return "$prefix-${System.currentTimeMillis() % 100000}-${notificationCounter.incrementAndGet()}"
    }

    val allComplaints: StateFlow<List<Complaint>> = complaintRepository.allComplaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredComplaints: StateFlow<List<Complaint>> = combine(
        complaintRepository.allComplaints,
        _filterState,
        currentUser
    ) { complaints, filter, user ->
        complaints.filter { complaint ->
            val matchesScope = when (filter.scopeTab) {
                "MY_ROOM" -> {
                    val roll = user?.rollNumber.orEmpty()
                    val room = user?.roomNumber.orEmpty()
                    val block = user?.hostelBlock.orEmpty()
                    val matchesRoll = roll.isNotBlank() && complaint.studentRoll.equals(roll, ignoreCase = true)
                    val matchesRoom = block.isNotBlank() && room.isNotBlank() &&
                            complaint.block.equals(block, ignoreCase = true) &&
                            complaint.roomNumber.equals(room, ignoreCase = true)
                    matchesRoll || matchesRoom
                }
                "ASSIGNED_ME" -> {
                    val staffName = user?.name.orEmpty()
                    val staffId = user?.rollNumber.orEmpty()
                    val trade = user?.assignedTrade
                    complaint.status != ComplaintStatus.REJECTED && (
                        (staffName.isNotBlank() && complaint.assignedTo?.contains(staffName, ignoreCase = true) == true) ||
                        (staffId.isNotBlank() && complaint.assignedTo?.contains(staffId, ignoreCase = true) == true) ||
                        (trade != null && complaint.aiAnalysis.category == trade)
                    )
                }
                "UNASSIGNED" -> complaint.assignedTo.isNullOrBlank() && complaint.status == ComplaintStatus.PENDING
                "BLOCK_FEED" -> {
                    val block = user?.hostelBlock ?: "BH-4"
                    complaint.block.equals(block, ignoreCase = true)
                }
                else -> true
            }

            val matchesTab = when (filter.statusTab) {
                "PENDING" -> complaint.status == ComplaintStatus.PENDING
                "IN_PROGRESS" -> complaint.status == ComplaintStatus.IN_PROGRESS || complaint.status == ComplaintStatus.ASSIGNED
                "RESOLVED" -> complaint.status == ComplaintStatus.RESOLVED
                "UNASSIGNED" -> complaint.assignedTo.isNullOrBlank() && complaint.status == ComplaintStatus.PENDING
                "CRITICAL" -> complaint.aiAnalysis.priority == ComplaintPriority.CRITICAL || complaint.aiAnalysis.priority == ComplaintPriority.HIGH
                else -> true
            }
            val matchesCategory = filter.selectedCategory == null || complaint.aiAnalysis.category == filter.selectedCategory
            val matchesPriority = filter.selectedPriority == null || complaint.aiAnalysis.priority == filter.selectedPriority
            val matchesBlock = filter.selectedBlock == null || complaint.block.equals(filter.selectedBlock, ignoreCase = true)
            val matchesSearch = filter.searchQuery.isBlank() ||
                    complaint.title.contains(filter.searchQuery, ignoreCase = true) ||
                    complaint.description.contains(filter.searchQuery, ignoreCase = true) ||
                    complaint.block.contains(filter.searchQuery, ignoreCase = true) ||
                    complaint.id.contains(filter.searchQuery, ignoreCase = true) ||
                    complaint.studentName.contains(filter.searchQuery, ignoreCase = true) ||
                    (complaint.assignedTo?.contains(filter.searchQuery, ignoreCase = true) == true)

            matchesScope && matchesTab && matchesCategory && matchesPriority && matchesBlock && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<ComplaintStats> = combine(
        complaintRepository.allComplaints,
        currentUser
    ) { complaints, user ->
        val staffName = user?.name.orEmpty()
        val staffId = user?.rollNumber.orEmpty()
        val staffTrade = user?.assignedTrade
        ComplaintStats(
            total = complaints.size,
            pending = complaints.count { it.status == ComplaintStatus.PENDING },
            inProgress = complaints.count { it.status == ComplaintStatus.IN_PROGRESS || it.status == ComplaintStatus.ASSIGNED },
            resolved = complaints.count { it.status == ComplaintStatus.RESOLVED },
            highPriority = complaints.count { it.aiAnalysis.priority == ComplaintPriority.HIGH || it.aiAnalysis.priority == ComplaintPriority.CRITICAL },
            unassignedCount = complaints.count { it.assignedTo.isNullOrBlank() && it.status == ComplaintStatus.PENDING },
            myAssignedCount = complaints.count {
                it.status != ComplaintStatus.REJECTED && (
                    (staffName.isNotBlank() && it.assignedTo?.contains(staffName, ignoreCase = true) == true) ||
                    (staffId.isNotBlank() && it.assignedTo?.contains(staffId, ignoreCase = true) == true) ||
                    (staffTrade != null && it.aiAnalysis.category == staffTrade)
                )
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ComplaintStats())

    init {
        viewModelScope.launch {
            complaintRepository.ensureInitialSeedData()
            complaintRepository.syncWithBackend()
        }
    }

    fun updateStatusTab(tab: String) {
        _filterState.value = _filterState.value.copy(statusTab = tab)
    }

    fun updateScopeTab(scope: String) {
        _filterState.value = _filterState.value.copy(scopeTab = scope)
    }

    fun updateCategoryFilter(cat: ComplaintCategory?) {
        _filterState.value = _filterState.value.copy(selectedCategory = cat)
    }

    fun updatePriorityFilter(priority: ComplaintPriority?) {
        _filterState.value = _filterState.value.copy(selectedPriority = priority)
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun updateBlockFilter(block: String?) {
        _filterState.value = _filterState.value.copy(selectedBlock = block)
    }

    fun updateUserHostelBlock(block: String, room: String) {
        authRepository.updateProfile(
            name = currentUser.value?.name ?: "Resident Student",
            phone = currentUser.value?.phone ?: "+91 98765 43210",
            block = block,
            room = room
        )
        _actionMessage.value = "Hostel block updated to $block (Room $room)"
    }

    fun onComplaintInputChanged(title: String, description: String, block: String) {
        if (title.isBlank() && description.isBlank()) {
            _liveAiState.value = LiveAiState()
            return
        }
        viewModelScope.launch {
            _liveAiState.value = _liveAiState.value.copy(isAnalyzing = true)
            val currentComplaints = allComplaints.value
            val analysis = HostelFixAIEngine.runFullPipeline(title, description, block, currentComplaints)
            val duplicate = HostelFixAIEngine.detectDuplicates(title, description, block, currentComplaints)
            _liveAiState.value = LiveAiState(
                category = analysis.category,
                priority = analysis.priority,
                department = analysis.department,
                sentiment = analysis.sentiment,
                confidence = analysis.confidence,
                duplicateInfo = duplicate,
                isAnalyzing = false
            )
        }
    }

    fun createComplaint(
        title: String,
        description: String,
        block: String,
        roomNumber: String,
        photoUri: String? = null,
        onSuccess: (String) -> Unit
    ) {
        val user = currentUser.value
        val studentName = user?.name ?: "Resident Student"
        val studentRoll = user?.rollNumber ?: "CS22B104"

        viewModelScope.launch {
            val created = complaintRepository.createComplaint(
                title = title,
                description = description,
                block = block,
                roomNumber = roomNumber,
                studentName = studentName,
                studentRoll = studentRoll,
                photoUri = photoUri
            )

            // Add notification
            notificationRepository.addNotification(
                AppNotification(
                    id = nextNotificationId("NOTIF"),
                    complaintId = created.id,
                    title = "Complaint Registered (${created.id})",
                    message = "AI classified as ${created.aiAnalysis.category.displayName} [${created.aiAnalysis.priority.label} Priority] routed to ${created.aiAnalysis.department}.",
                    timestamp = System.currentTimeMillis(),
                    type = NotificationType.STATUS_UPDATE
                )
            )

            if (created.duplicateInfo?.isDuplicate == true) {
                notificationRepository.addNotification(
                    AppNotification(
                        id = nextNotificationId("NOTIF-DUP"),
                        complaintId = created.id,
                        title = "Potential Duplicate Detected",
                        message = "Similar complaint ${created.duplicateInfo.similarComplaintId} (${(created.duplicateInfo.similarityScore * 100).toInt()}% similarity) was already filed in $block.",
                        timestamp = System.currentTimeMillis() + 100,
                        type = NotificationType.DUPLICATE_ALERT
                    )
                )
            }

            _actionMessage.value = "Complaint ${created.id} submitted successfully!"
            onSuccess(created.id)
        }
    }

    fun updateComplaintStatus(
        complaintId: String,
        newStatus: ComplaintStatus,
        assignedStaff: String? = null,
        note: String? = null
    ) {
        viewModelScope.launch {
            complaintRepository.updateStatus(complaintId, newStatus, assignedStaff, note)
            notificationRepository.addNotification(
                AppNotification(
                    id = nextNotificationId("NOTIF"),
                    complaintId = complaintId,
                    title = "Status: ${newStatus.label}",
                    message = note ?: (if (assignedStaff != null) "Assigned to $assignedStaff" else "Complaint $complaintId updated to ${newStatus.label}"),
                    timestamp = System.currentTimeMillis(),
                    type = if (newStatus == ComplaintStatus.RESOLVED) NotificationType.RESOLVED else NotificationType.STATUS_UPDATE
                )
            )
            _actionMessage.value = "Status updated to ${newStatus.label}"
        }
    }

    fun addMaintenanceLogNote(complaintId: String, note: String) {
        if (note.isBlank()) return
        viewModelScope.launch {
            complaintRepository.addWorkNote(complaintId, note)
            notificationRepository.addNotification(
                AppNotification(
                    id = nextNotificationId("NOTIF-NOTE"),
                    complaintId = complaintId,
                    title = "New Maintenance Log Entry",
                    message = note,
                    timestamp = System.currentTimeMillis(),
                    type = NotificationType.STATUS_UPDATE
                )
            )
            _actionMessage.value = "Maintenance log note added"
        }
    }

    fun submitFeedback(complaintId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            complaintRepository.submitFeedback(complaintId, rating, comment)
            _actionMessage.value = "Thank you! Feedback recorded."
        }
    }

    private fun applyDefaultFilterForRole(role: UserRole) {
        when (role) {
            UserRole.STUDENT -> {
                _filterState.value = ComplaintFilterState(scopeTab = "MY_ROOM", statusTab = "ALL")
            }
            UserRole.WARDEN -> {
                _filterState.value = ComplaintFilterState(scopeTab = "ALL", statusTab = "UNASSIGNED")
            }
            UserRole.STAFF -> {
                _filterState.value = ComplaintFilterState(scopeTab = "ASSIGNED_ME", statusTab = "ALL")
            }
        }
    }

    fun login(
        email: String,
        pass: String,
        role: UserRole,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = authRepository.loginWithRole(email, pass, role)
            if (result.isSuccess) {
                val user = result.getOrNull()
                applyDefaultFilterForRole(role)
                _actionMessage.value = "Welcome, ${user?.name} (${role.displayName})!"
                onSuccess()
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Login failed. Please check credentials."
                onError(msg)
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _filterState.value = ComplaintFilterState()
        _liveAiState.value = LiveAiState()
        _actionMessage.value = "Logged out successfully"
    }

    fun switchRole(role: UserRole) {
        authRepository.switchRole(role)
        applyDefaultFilterForRole(role)
        _actionMessage.value = "Switched to ${role.displayName} view"
    }

    fun assignStaff(complaintId: String, staffName: String, staffRoleTitle: String) {
        val user = currentUser.value
        val wardenName = user?.name ?: "Duty Warden"
        updateComplaintStatus(
            complaintId = complaintId,
            newStatus = ComplaintStatus.ASSIGNED,
            assignedStaff = "$staffName ($staffRoleTitle)",
            note = "Assigned to $staffName ($staffRoleTitle) by $wardenName"
        )
        _actionMessage.value = "Work order assigned to $staffName"
    }

    fun startWorkOrder(complaintId: String) {
        val user = currentUser.value
        val staffName = user?.name ?: "Maintenance Technician"
        updateComplaintStatus(
            complaintId = complaintId,
            newStatus = ComplaintStatus.IN_PROGRESS,
            assignedStaff = staffName,
            note = "Repair work started on site by $staffName"
        )
        _actionMessage.value = "Maintenance work started on $complaintId"
    }

    fun completeWorkOrder(complaintId: String, notes: String = "") {
        val user = currentUser.value
        val staffName = user?.name ?: "Maintenance Technician"
        val resolvedNote = if (notes.isNotBlank()) notes else "Work verified and completed by $staffName"
        updateComplaintStatus(
            complaintId = complaintId,
            newStatus = ComplaintStatus.RESOLVED,
            assignedStaff = staffName,
            note = resolvedNote
        )
        _actionMessage.value = "Complaint $complaintId marked as RESOLVED"
    }

    fun cancelStudentComplaint(complaintId: String) {
        val user = currentUser.value
        val studentName = user?.name ?: "Resident Student"
        updateComplaintStatus(
            complaintId = complaintId,
            newStatus = ComplaintStatus.REJECTED,
            assignedStaff = null,
            note = "Request withdrawn by $studentName"
        )
        _actionMessage.value = "Complaint $complaintId has been withdrawn"
    }

    fun rejectWardenComplaint(complaintId: String, reason: String) {
        val user = currentUser.value
        val wardenName = user?.name ?: "Warden Desk"
        updateComplaintStatus(
            complaintId = complaintId,
            newStatus = ComplaintStatus.REJECTED,
            assignedStaff = null,
            note = "Rejected by $wardenName: $reason"
        )
        _actionMessage.value = "Complaint rejected: $reason"
    }

    fun updateBackendUrl(url: String) {
        if (ApiClient.updateBaseUrl(url)) {
            _backendUrl.value = ApiClient.getBaseUrl()
            _actionMessage.value = "Backend API configured to ${ApiClient.getBaseUrl()}"
            viewModelScope.launch {
                complaintRepository.syncWithBackend()
            }
        } else {
            _actionMessage.value = "Please enter a valid HTTP or HTTPS server URL"
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
