package com.example.data.model

data class AppNotification(
    val id: String,
    val complaintId: String?,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.STATUS_UPDATE
)

enum class NotificationType {
    STATUS_UPDATE,
    STAFF_ASSIGNED,
    DUPLICATE_ALERT,
    RESOLVED,
    ANNOUNCEMENT
}
