package com.hostelfix.data.model

import com.google.gson.annotations.SerializedName

enum class UserRole {
    @SerializedName("STUDENT")
    STUDENT,
    @SerializedName("STAFF")
    STAFF,
    @SerializedName("ADMIN")
    ADMIN
}

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("hostelBlock") val hostelBlock: String,
    @SerializedName("roomNumber") val roomNumber: String,
    @SerializedName("role") val role: UserRole,
    @SerializedName("isLocationVerified") val isLocationVerified: Boolean,
    @SerializedName("verifiedAt") val verifiedAt: String?,
    @SerializedName("createdAt") val createdAt: String?
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("hostelBlock") val hostelBlock: String,
    @SerializedName("roomNumber") val roomNumber: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user") val user: UserDto
)
