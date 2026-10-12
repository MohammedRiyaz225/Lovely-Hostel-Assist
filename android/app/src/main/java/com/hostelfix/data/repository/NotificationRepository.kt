package com.hostelfix.data.repository

import com.hostelfix.data.api.ApiService
import com.hostelfix.data.model.NotificationDto
import com.hostelfix.data.model.Resource

class NotificationRepository(
    private val apiService: ApiService
) {

    suspend fun getNotifications(): Resource<List<NotificationDto>> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to fetch notifications: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch notifications", e)
        }
    }

    suspend fun markRead(notificationId: String): Resource<Boolean> {
        return try {
            val response = apiService.markNotificationRead(notificationId)
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Failed to mark notification read")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error", e)
        }
    }
}
