package com.example.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserDto,
    val message: String = "Login successful"
)

data class UserDto(
    val id: String,
    val name: String,
    val roll_number: String?,
    val email: String,
    val hostel_block: String?,
    val room_number: String?,
    val role: String
)

data class ComplaintRequest(
    val title: String,
    val description: String,
    val block: String,
    val room_number: String,
    val category: String?,
    val priority: String?,
    val image_url: String? = null
)

data class ComplaintResponse(
    val id: String,
    val title: String,
    val description: String,
    val block: String,
    val room_number: String? = null,
    val student_name: String? = null,
    val student_roll: String? = null,
    val status: String,
    val assigned_to: String? = null,
    val created_at: String? = null,
    val ai: AiDto? = null,
    val duplicate: DuplicateDto? = null
)

data class AiDto(
    val category: String?,
    val priority: String?,
    val department: String?,
    val sentiment: String?,
    val confidence: Float?,
    val summary: String?
)

data class DuplicateDto(
    val is_duplicate: Boolean,
    val similar_complaint_id: String?,
    val similarity: Float?
)

data class StatusUpdateRequest(
    val status: String,
    val assigned_to: String? = null,
    val note: String? = null
)

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("api/complaints")
    suspend fun createComplaint(
        @Body complaint: ComplaintRequest
    ): ComplaintResponse

    @GET("api/complaints")
    suspend fun getComplaints(): List<ComplaintResponse>

    @GET("api/complaints/{id}")
    suspend fun getComplaint(
        @Path("id") id: String
    ): ComplaintResponse

    @PUT("api/complaints/{id}/status")
    suspend fun updateComplaintStatus(
        @Path("id") id: String,
        @Body request: StatusUpdateRequest
    ): ComplaintResponse
}
