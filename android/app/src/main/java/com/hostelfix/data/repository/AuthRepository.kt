package com.hostelfix.data.repository

import com.hostelfix.data.api.ApiService
import com.hostelfix.data.local.TokenManager
import com.hostelfix.data.model.LoginRequest
import com.hostelfix.data.model.LoginResponse
import com.hostelfix.data.model.RegisterRequest
import com.hostelfix.data.model.Resource
import com.hostelfix.data.model.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    val isLocationVerifiedFlow: Flow<Boolean> = tokenManager.isLocationVerifiedFlow
    val userBlockFlow: Flow<String?> = tokenManager.userBlockFlow
    val userNameFlow: Flow<String?> = tokenManager.userNameFlow

    suspend fun login(request: LoginRequest): Resource<LoginResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val loginData = response.body()!!
                tokenManager.saveAuthData(
                    token = loginData.accessToken,
                    id = loginData.user.id,
                    name = loginData.user.name,
                    email = loginData.user.email,
                    phone = loginData.user.phone,
                    block = loginData.user.hostelBlock,
                    room = loginData.user.roomNumber,
                    isVerified = loginData.user.isLocationVerified
                )
                Resource.Success(loginData)
            } else {
                val err = response.errorBody()?.string() ?: "Login failed"
                Resource.Error(err)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error occurred", e)
        }
    }

    suspend fun register(request: RegisterRequest): Resource<LoginResponse> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val loginData = response.body()!!
                tokenManager.saveAuthData(
                    token = loginData.accessToken,
                    id = loginData.user.id,
                    name = loginData.user.name,
                    email = loginData.user.email,
                    phone = loginData.user.phone,
                    block = loginData.user.hostelBlock,
                    room = loginData.user.roomNumber,
                    isVerified = loginData.user.isLocationVerified
                )
                Resource.Success(loginData)
            } else {
                val err = response.errorBody()?.string() ?: "Registration failed"
                Resource.Error(err)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error occurred", e)
        }
    }

    suspend fun getProfile(): Resource<UserDto> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                tokenManager.setLocationVerified(user.isLocationVerified, user.hostelBlock)
                Resource.Success(user)
            } else {
                Resource.Error("Failed to fetch profile: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error", e)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val token = tokenManager.getToken()
        return !token.isNullOrBlank()
    }

    suspend fun logout() {
        tokenManager.clearSession()
    }
}
