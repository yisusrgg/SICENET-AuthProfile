package com.example.sicenet_authprofile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sicenet_authprofile.data.model.UserProfile
import com.example.sicenet_authprofile.data.repository.SicenetRepository
import com.example.sicenet_authprofile.data.repository.SicenetRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val cookie: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class SicenetViewModel(
    private val repository: SicenetRepository = SicenetRepositoryImpl()
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    fun login(user: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            val result = repository.login(user, pass)
            if (result.success && result.cookie != null) {
                _loginState.value = LoginUiState.Success(result.cookie)
            } else {
                _loginState.value = LoginUiState.Error(result.message ?: "Error desconocido")
            }
        }
    }

    fun getProfile(cookie: String) {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            val profile = repository.getUserProfile(cookie)
            if (profile != null) {
                _profileState.value = ProfileUiState.Success(profile)
            } else {
                _profileState.value = ProfileUiState.Error("No se pudo cargar el perfil")
            }
        }
    }
    
    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
    }
}
