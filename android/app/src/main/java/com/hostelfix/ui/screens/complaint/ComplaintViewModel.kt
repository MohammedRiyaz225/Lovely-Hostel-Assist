package com.hostelfix.ui.screens.complaint

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.hostelfix.data.local.TokenManager
import com.hostelfix.data.model.ComplaintDto
import com.hostelfix.data.model.Resource
import com.hostelfix.data.repository.ComplaintRepository
import com.hostelfix.data.repository.HostelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class ComplaintUiState(
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val complaintDetail: ComplaintDto? = null,
    val isSubmittingFeedback: Boolean = false,
    val feedbackSuccess: Boolean = false,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val isInsideRadius: Boolean? = null,
    val errorMessage: String? = null
)

class ComplaintViewModel(
    private val complaintRepository: ComplaintRepository,
    private val hostelRepository: HostelRepository,
    tokenManager: TokenManager,
    context: Context
) : ViewModel() {

    val userBlock: StateFlow<String?> = tokenManager.userBlockFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BH-1")

    val userRoom: StateFlow<String?> = tokenManager.userRoomFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val isUserLocationVerified: StateFlow<Boolean> = tokenManager.isLocationVerifiedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    private val _uiState = MutableStateFlow(ComplaintUiState())
    val uiState: StateFlow<ComplaintUiState> = _uiState.asStateFlow()

    @SuppressLint("MissingPermission")
    fun fetchGpsForComplaint(block: String) {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    _uiState.value = _uiState.value.copy(
                        currentLat = loc.latitude,
                        currentLng = loc.longitude
                    )
                    // Check if inside hostel block radius
                    checkIfInsideHostel(block, loc.latitude, loc.longitude)
                }
            }
    }

    private fun checkIfInsideHostel(block: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            when (val hostelsRes = hostelRepository.getHostels()) {
                is Resource.Success -> {
                    val hostel = hostelsRes.data.firstOrNull { it.block.equals(block, ignoreCase = true) }
                    if (hostel != null) {
                        val distance = FloatArray(1)
                        Location.distanceBetween(lat, lng, hostel.latitude, hostel.longitude, distance)
                        val inside = distance[0] <= hostel.radiusMeters
                        _uiState.value = _uiState.value.copy(isInsideRadius = inside)
                    }
                }
                else -> {}
            }
        }
    }

    fun submitComplaint(
        title: String,
        description: String,
        hostelBlock: String,
        roomNumber: String,
        imageFile: File?
    ) {
        if (title.isBlank() || description.isBlank() || hostelBlock.isBlank() || roomNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            val result = complaintRepository.submitComplaint(
                title = title.trim(),
                description = description.trim(),
                hostelBlock = hostelBlock.trim(),
                roomNumber = roomNumber.trim(),
                submittedLat = _uiState.value.currentLat,
                submittedLng = _uiState.value.currentLng,
                imageFile = imageFile
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submitSuccess = true,
                        complaintDetail = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = result.message
                    )
                }
                Resource.Loading -> {}
            }
        }
    }

    fun loadComplaintDetail(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDetail = true, errorMessage = null)
            when (val res = complaintRepository.getComplaintById(id)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingDetail = false,
                        complaintDetail = res.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingDetail = false,
                        errorMessage = res.message
                    )
                }
                Resource.Loading -> {}
            }
        }
    }

    fun submitFeedback(complaintId: String, rating: Int, comment: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingFeedback = true, errorMessage = null)
            when (val res = complaintRepository.submitFeedback(complaintId, rating, comment)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmittingFeedback = false,
                        feedbackSuccess = true,
                        complaintDetail = res.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmittingFeedback = false,
                        errorMessage = res.message
                    )
                }
                Resource.Loading -> {}
            }
        }
    }

    fun resetSubmissionState() {
        _uiState.value = _uiState.value.copy(
            submitSuccess = false,
            errorMessage = null,
            feedbackSuccess = false
        )
    }
}

class ComplaintViewModelFactory(
    private val complaintRepository: ComplaintRepository,
    private val hostelRepository: HostelRepository,
    private val tokenManager: TokenManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComplaintViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ComplaintViewModel(complaintRepository, hostelRepository, tokenManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
