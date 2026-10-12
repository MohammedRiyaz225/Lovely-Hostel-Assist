package com.hostelfix.data.api

import com.hostelfix.data.model.ComplaintDto
import com.hostelfix.data.model.ComplaintStatsDto
import com.hostelfix.data.model.FeedbackRequest
import com.hostelfix.data.model.HostelDto
import com.hostelfix.data.model.LoginRequest
import com.hostelfix.data.model.LoginResponse
import com.hostelfix.data.model.NotificationDto
import com.hostelfix.data.model.RegisterRequest
import com.hostelfix.data.model.UserDto
import com.hostelfix.data.model.VerifyLocationRequest
import com.hostelfix.data.model.VerifyLocationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<UserDto>

    // Hostels & Location
    @GET("api/hostels")
    suspend fun getHostels(): Response<List<HostelDto>>

    @POST("api/hostels/verify")
    suspend fun verifyLocation(@Body request: VerifyLocationRequest): Response<VerifyLocationResponse>

    // Complaints
    @Multipart
    @POST("api/complaints")
    suspend fun submitComplaint(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("hostelBlock") hostelBlock: RequestBody,
        @Part("roomNumber") roomNumber: RequestBody,
        @Part("submittedLat") submittedLat: RequestBody?,
        @Part("submittedLng") submittedLng: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<ComplaintDto>

    @GET("api/complaints")
    suspend fun getComplaints(): Response<List<ComplaintDto>>

    @GET("api/complaints/{id}")
    suspend fun getComplaintById(@Path("id") id: String): Response<ComplaintDto>

    @GET("api/complaints/stats")
    suspend fun getComplaintStats(): Response<ComplaintStatsDto>

    @POST("api/complaints/{id}/feedback")
    suspend fun submitFeedback(
        @Path("id") id: String,
        @Body feedback: FeedbackRequest
    ): Response<ComplaintDto>

    // Notifications
    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @PATCH("api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<Map<String, Boolean>>
}
