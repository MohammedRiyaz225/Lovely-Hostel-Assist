package com.hostelfix.data.api

import com.hostelfix.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Bypass for registration and login
        val path = originalRequest.url.encodedPath
        if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenManager.getToken() }
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
