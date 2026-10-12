package com.hostelfix

import android.app.Application
import com.hostelfix.data.api.ApiClient
import com.hostelfix.data.api.ApiService
import com.hostelfix.data.local.TokenManager
import com.hostelfix.data.repository.AuthRepository
import com.hostelfix.data.repository.ComplaintRepository
import com.hostelfix.data.repository.HostelRepository
import com.hostelfix.data.repository.NotificationRepository

class HostelFixApp : Application() {

    lateinit var tokenManager: TokenManager
        private set

    lateinit var apiService: ApiService
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var hostelRepository: HostelRepository
        private set

    lateinit var complaintRepository: ComplaintRepository
        private set

    lateinit var notificationRepository: NotificationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        tokenManager = TokenManager(this)
        apiService = ApiClient.getService(this)

        authRepository = AuthRepository(apiService, tokenManager)
        hostelRepository = HostelRepository(apiService, tokenManager)
        complaintRepository = ComplaintRepository(apiService)
        notificationRepository = NotificationRepository(apiService)
    }

    companion object {
        lateinit var instance: HostelFixApp
            private set
    }
}
