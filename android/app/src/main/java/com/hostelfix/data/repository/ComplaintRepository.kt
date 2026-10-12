package com.hostelfix.data.repository

import com.hostelfix.data.api.ApiService
import com.hostelfix.data.model.ComplaintDto
import com.hostelfix.data.model.ComplaintStatsDto
import com.hostelfix.data.model.FeedbackRequest
import com.hostelfix.data.model.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ComplaintRepository(
    private val apiService: ApiService
) {

    suspend fun submitComplaint(
        title: String,
        description: String,
        hostelBlock: String,
        roomNumber: String,
        submittedLat: Double?,
        submittedLng: Double?,
        imageFile: File?
    ): Resource<ComplaintDto> {
        return try {
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val blockPart = hostelBlock.toRequestBody("text/plain".toMediaTypeOrNull())
            val roomPart = roomNumber.toRequestBody("text/plain".toMediaTypeOrNull())

            val latPart = submittedLat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val lngPart = submittedLng?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imageFile?.let { file ->
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", file.name, requestFile)
            }

            val response = apiService.submitComplaint(
                title = titlePart,
                description = descPart,
                hostelBlock = blockPart,
                roomNumber = roomPart,
                submittedLat = latPart,
                submittedLng = lngPart,
                image = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to submit complaint"
                Resource.Error(err)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error while submitting complaint", e)
        }
    }

    suspend fun getComplaints(): Resource<List<ComplaintDto>> {
        return try {
            val response = apiService.getComplaints()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to fetch complaints: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load complaints", e)
        }
    }

    suspend fun getComplaintById(id: String): Resource<ComplaintDto> {
        return try {
            val response = apiService.getComplaintById(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to fetch complaint: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load complaint details", e)
        }
    }

    suspend fun getComplaintStats(): Resource<ComplaintStatsDto> {
        return try {
            val response = apiService.getComplaintStats()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to fetch stats: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load stats", e)
        }
    }

    suspend fun submitFeedback(complaintId: String, rating: Int, comment: String?): Resource<ComplaintDto> {
        return try {
            val req = FeedbackRequest(rating = rating, comment = comment)
            val response = apiService.submitFeedback(complaintId, req)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to submit feedback"
                Resource.Error(err)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error while submitting feedback", e)
        }
    }
}
