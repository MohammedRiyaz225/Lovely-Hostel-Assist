package com.hostelfix.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hostelfix.data.local.TokenManager
import com.hostelfix.data.model.ComplaintDto
import com.hostelfix.data.model.ComplaintStatsDto
import com.hostelfix.data.model.Resource
import com.hostelfix.data.repository.AuthRepository
import com.hostelfix.data.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val stats: ComplaintStatsDto? = null,
    val recentComplaints: List<ComplaintDto> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val complaintRepository: ComplaintRepository,
    private val authRepository: AuthRepository,
    tokenManager: TokenManager
) : ViewModel() {

    val isLocationVerified: StateFlow<Boolean> = tokenManager.isLocationVerifiedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userName: StateFlow<String?> = tokenManager.userNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Student")

    val userBlock: StateFlow<String?> = tokenManager.userBlockFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "LPU")

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Refresh user profile in background
            authRepository.getProfile()

            // Fetch Stats
            val statsResult = complaintRepository.getComplaintStats()
            val complaintsResult = complaintRepository.getComplaints()

            var newStats = _uiState.value.stats
            var newComplaints = _uiState.value.recentComplaints

            if (statsResult is Resource.Success) {
                newStats = statsResult.data
            }

            if (complaintsResult is Resource.Success) {
                newComplaints = complaintsResult.data.take(5)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                stats = newStats,
                recentComplaints = newComplaints
            )
        }
    }
}

class HomeViewModelFactory(
    private val complaintRepository: ComplaintRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(complaintRepository, authRepository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
