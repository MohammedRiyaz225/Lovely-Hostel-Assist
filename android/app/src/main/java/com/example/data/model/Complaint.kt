package com.example.data.model

data class Complaint(
    val id: String,
    val title: String,
    val description: String,
    val block: String,
    val roomNumber: String = "",
    val studentName: String = "",
    val studentRoll: String = "",
    val status: ComplaintStatus = ComplaintStatus.PENDING,
    val assignedTo: String? = null,
    val assignedStaffRole: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val photoUri: String? = null,
    val aiAnalysis: AIAnalysis,
    val duplicateInfo: DuplicateInfo? = null,
    val studentFeedback: StudentFeedback? = null,
    val statusHistory: List<StatusStep> = emptyList(),
    val workNotes: String? = null
)

data class AIAnalysis(
    val category: ComplaintCategory,
    val priority: ComplaintPriority,
    val department: String,
    val sentiment: SentimentType,
    val confidence: Float,
    val summary: String,
    val estimatedResolutionHours: Int = 24
)

data class DuplicateInfo(
    val isDuplicate: Boolean,
    val similarComplaintId: String? = null,
    val similarComplaintTitle: String? = null,
    val similarityScore: Float = 0.0f
)

data class StudentFeedback(
    val rating: Int, // 1 to 5
    val comment: String = ""
)

data class StatusStep(
    val status: ComplaintStatus,
    val timestamp: Long,
    val note: String = ""
)

enum class ComplaintStatus(val label: String) {
    PENDING("Pending"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    REJECTED("Rejected")
}

enum class ComplaintCategory(val displayName: String) {
    PLUMBING("Plumbing"),
    ELECTRICAL("Electrical"),
    CLEANING("Cleaning"),
    WIFI("Wi-Fi & Network"),
    FOOD("Food & Mess"),
    FURNITURE("Furniture"),
    SECURITY("Security"),
    GENERAL("General Maintenance")
}

enum class ComplaintPriority(val label: String, val weight: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}

enum class SentimentType(val label: String) {
    POSITIVE("Positive"),
    NEUTRAL("Neutral"),
    NEGATIVE("Negative")
}
