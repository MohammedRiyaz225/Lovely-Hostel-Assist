package com.hostelfix.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hostelfix.data.model.HostelDto
import com.hostelfix.data.model.LoginRequest
import com.hostelfix.data.model.LoginResponse
import com.hostelfix.data.model.RegisterRequest
import com.hostelfix.data.model.Resource
import com.hostelfix.data.repository.AuthRepository
import com.hostelfix.data.repository.HostelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val registerSuccess: Boolean = false,
    val errorMessage: String? = null,
    val hostels: List<HostelDto> = emptyList()
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val hostelRepository: HostelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        loadHostels()
    }

    fun loadHostels() {
        viewModelScope.launch {
            when (val res = hostelRepository.getHostels()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(hostels = res.data)
                }
                is Resource.Error -> {
                    // Non-fatal, use fallback if any
                }
                Resource.Loading -> {}
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.login(LoginRequest(email.trim(), pass))
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> {}
            }
        }
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        phone: String,
        block: String,
        room: String
    ) {
        if (name.isBlank() || email.isBlank() || pass.isBlank() || phone.isBlank() || block.isBlank() || room.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "All fields are required")
            return
        }

        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val req = RegisterRequest(
                name = name.trim(),
                email = email.trim(),
                password = pass,
                phone = phone.trim(),
                hostelBlock = block,
                roomNumber = room.trim()
            )
            val result = authRepository.register(req)
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, registerSuccess = true)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun checkAuthStatus(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            onResult(loggedIn)
        }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val hostelRepository: HostelRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository, hostelRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
