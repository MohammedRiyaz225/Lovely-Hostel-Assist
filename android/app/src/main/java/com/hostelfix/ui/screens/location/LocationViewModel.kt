package com.hostelfix.ui.screens.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.hostelfix.data.model.HostelDto
import com.hostelfix.data.model.Resource
import com.hostelfix.data.model.VerifyLocationResponse
import com.hostelfix.data.repository.HostelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationUiState(
    val isLoadingHostels: Boolean = false,
    val isFetchingGps: Boolean = false,
    val isVerifying: Boolean = false,
    val hostels: List<HostelDto> = emptyList(),
    val selectedHostel: HostelDto? = null,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val accuracy: Float? = null,
    val verificationResponse: VerifyLocationResponse? = null,
    val errorMessage: String? = null
)

class LocationViewModel(
    private val hostelRepository: HostelRepository,
    context: Context
) : ViewModel() {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        loadHostels()
    }

    fun loadHostels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingHostels = true)
            when (val res = hostelRepository.getHostels()) {
                is Resource.Success -> {
                    val list = res.data
                    _uiState.value = _uiState.value.copy(
                        isLoadingHostels = false,
                        hostels = list,
                        selectedHostel = list.firstOrNull()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingHostels = false,
                        errorMessage = res.message
                    )
                }
                Resource.Loading -> {}
            }
        }
    }

    fun selectHostel(hostel: HostelDto) {
        _uiState.value = _uiState.value.copy(selectedHostel = hostel)
    }

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation() {
        _uiState.value = _uiState.value.copy(isFetchingGps = true, errorMessage = null)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    _uiState.value = _uiState.value.copy(
                        isFetchingGps = false,
                        currentLat = location.latitude,
                        currentLng = location.longitude,
                        accuracy = location.accuracy
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isFetchingGps = false,
                        errorMessage = "Unable to get GPS fix. Please ensure location services are enabled."
                    )
                }
            }
            .addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isFetchingGps = false,
                    errorMessage = "Location error: ${exception.localizedMessage}"
                )
            }
    }

    fun verifyLocation() {
        val state = _uiState.value
        val hostel = state.selectedHostel
        val lat = state.currentLat
        val lng = state.currentLng
        val accuracy = state.accuracy ?: 10f

        if (hostel == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please select a hostel block")
            return
        }

        if (lat == null || lng == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Waiting for current GPS location...")
            requestCurrentLocation()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true, errorMessage = null)
            val result = hostelRepository.verifyLocation(
                hostelBlock = hostel.block,
                latitude = lat,
                longitude = lng,
                accuracy = accuracy
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isVerifying = false,
                        verificationResponse = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isVerifying = false,
                        errorMessage = result.message
                    )
                }
                Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class LocationViewModelFactory(
    private val hostelRepository: HostelRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(hostelRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
