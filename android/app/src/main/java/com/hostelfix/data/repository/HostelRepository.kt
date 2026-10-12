package com.hostelfix.data.repository

import com.hostelfix.data.api.ApiService
import com.hostelfix.data.local.TokenManager
import com.hostelfix.data.model.HostelDto
import com.hostelfix.data.model.Resource
import com.hostelfix.data.model.VerifyLocationRequest
import com.hostelfix.data.model.VerifyLocationResponse

class HostelRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun getHostels(): Resource<List<HostelDto>> {
        return try {
            val response = apiService.getHostels()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to fetch hostels: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error occurred", e)
        }
    }

    suspend fun verifyLocation(
        hostelBlock: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float
    ): Resource<VerifyLocationResponse> {
        return try {
            val req = VerifyLocationRequest(
                hostelBlock = hostelBlock,
                latitude = latitude,
                longitude = longitude,
                accuracy = accuracy
            )
            val response = apiService.verifyLocation(req)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.verified) {
                    tokenManager.setLocationVerified(true, hostelBlock)
                }
                Resource.Success(res)
            } else {
                val err = response.errorBody()?.string() ?: "Verification failed"
                Resource.Error(err)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to verify location", e)
        }
    }
}
