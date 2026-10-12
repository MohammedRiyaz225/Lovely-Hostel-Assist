package com.hostelfix.data.model

import com.google.gson.annotations.SerializedName

enum class ComplaintStatus {
    @SerializedName("SUBMITTED")
    SUBMITTED,
    @SerializedName("AI_ANALYZED")
    AI_ANALYZED,
    @SerializedName("ASSIGNED")
    ASSIGNED,
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,
    @SerializedName("RESOLVED")
    RESOLVED,
    @SerializedName("FEEDBACK")
    FEEDBACK,
    @SerializedName("CLOSED")
    CLOSED
}

data class AIPredictionDto(
    @SerializedName("category") val category: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("department") val department: String? = null,
    @SerializedName("sentiment") val sentiment: String? = null,
    @SerializedName("sentimentScore") val sentimentScore: Float? = null,
    @SerializedName("extractedLocation") val extractedLocation: String? = null,
    @SerializedName("isDuplicate") val isDuplicate: Boolean? = null,
    @SerializedName("duplicateOf") val duplicateOf: String? = null,
    @SerializedName("similarityScore") val similarityScore: Float? = null,
    @SerializedName("summary") val summary: String? = null
)

data class FeedbackDto(
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("submittedAt") val submittedAt: String? = null
)

data class FeedbackRequest(
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String? = null
)

data class ComplaintDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("hostelBlock") val hostelBlock: String,
    @SerializedName("roomNumber") val roomNumber: String,
    @SerializedName("status") val status: ComplaintStatus,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("submittedLat") val submittedLat: Double? = null,
    @SerializedName("submittedLng") val submittedLng: Double? = null,
    @SerializedName("locationVerified") val locationVerified: Boolean = false,
    @SerializedName("ai") val ai: AIPredictionDto? = null,
    @SerializedName("assignedTo") val assignedTo: String? = null,
    @SerializedName("adminComment") val adminComment: String? = null,
    @SerializedName("resolutionComment") val resolutionComment: String? = null,
    @SerializedName("feedback") val feedback: FeedbackDto? = null,
    @SerializedName("studentId") val studentId: String,
    @SerializedName("studentName") val studentName: String? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class ComplaintStatsDto(
    @SerializedName("total") val total: Int,
    @SerializedName("pending") val pending: Int,
    @SerializedName("resolved") val resolved: Int,
    @SerializedName("closed") val closed: Int
)

data class NotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("complaintId") val complaintId: String? = null,
    @SerializedName("message") val message: String,
    @SerializedName("read") val read: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null
)
