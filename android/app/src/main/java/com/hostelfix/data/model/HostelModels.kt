package com.hostelfix.data.model

import com.google.gson.annotations.SerializedName

data class HostelDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("block") val block: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("radiusMeters") val radiusMeters: Int
)

data class VerifyLocationRequest(
    @SerializedName("hostelBlock") val hostelBlock: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float
)

data class VerifyLocationResponse(
    @SerializedName("verified") val verified: Boolean,
    @SerializedName("distanceMeters") val distanceMeters: Double,
    @SerializedName("allowedRadius") val allowedRadius: Int,
    @SerializedName("message") val message: String
)
