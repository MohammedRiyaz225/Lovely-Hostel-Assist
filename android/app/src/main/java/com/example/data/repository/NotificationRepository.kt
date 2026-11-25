package com.example.data.repository

import com.example.data.model.AppNotification
import com.example.data.model.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationRepository {

    private val _notifications = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(
                id = "NOTIF-1",
                complaintId = "CMP-1021",
                title = "Plumber Assigned",
                message = "Staff member Ramesh Kumar (Plumber) has been assigned to your complaint in BH-4.",
                timestamp = System.currentTimeMillis() - 7200_000L,
                isRead = false,
                type = NotificationType.STAFF_ASSIGNED
            ),
            AppNotification(
                id = "NOTIF-2",
                complaintId = "CMP-1022",
                title = "AI Priority Escalation",
                message = "AI Engine classified your noise complaint as Medium Priority for Electrical Dept.",
                timestamp = System.currentTimeMillis() - 14400_000L,
                isRead = false,
                type = NotificationType.STATUS_UPDATE
            ),
            AppNotification(
                id = "NOTIF-3",
                complaintId = "CMP-1023",
                title = "Wi-Fi Issue Resolved",
                message = "Your Wi-Fi complaint in Wing C has been marked as Resolved. Please provide feedback.",
                timestamp = System.currentTimeMillis() - 18000_000L,
                isRead = true,
                type = NotificationType.RESOLVED
            )
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun addNotification(notification: AppNotification) {
        _notifications.value = listOf(notification) + _notifications.value
    }

    fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearAll() {
        _notifications.value = emptyList()
    }
}
