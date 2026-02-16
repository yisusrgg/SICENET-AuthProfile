package com.example.sicenet_authprofile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sicenet_authprofile.SicenetApplication
import com.example.sicenet_authprofile.data.model.CalificacionFinal
import com.example.sicenet_authprofile.data.model.CalificacionUnidad
import com.example.sicenet_authprofile.data.model.CardexItem
import com.example.sicenet_authprofile.data.model.PerfilAcademico
import com.example.sicenet_authprofile.data.repository.SicenetRepository
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
    data class Success(val profile: PerfilAcademico) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
sealed class CardexUiState {
    object Loading : CardexUiState()
    data class Success(val list: List<CardexItem>) : CardexUiState()
    data class Error(val message: String) : CardexUiState()
}

sealed class CalifFinalUiState {
    object Loading : CalifFinalUiState()
    data class Success(val list: List<CalificacionFinal>) : CalifFinalUiState()
    data class Error(val message: String) : CalifFinalUiState()
}

sealed class CalifUnidadUiState {
    object Loading : CalifUnidadUiState()
    data class Success(val list: List<CalificacionUnidad>) : CalifUnidadUiState()
    data class Error(val message: String) : CalifUnidadUiState()
}

class SicenetViewModel(
    private val repository: SicenetRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _cardexState = MutableStateFlow<CardexUiState>(CardexUiState.Loading)
    val cardexState: StateFlow<CardexUiState> = _cardexState.asStateFlow()

    private val _califFinalState = MutableStateFlow<CalifFinalUiState>(CalifFinalUiState.Loading)
    val califFinalState: StateFlow<CalifFinalUiState> = _califFinalState.asStateFlow()

    private val _califUnidadState = MutableStateFlow<CalifUnidadUiState>(CalifUnidadUiState.Loading)
    val califUnidadState: StateFlow<CalifUnidadUiState> = _califUnidadState.asStateFlow()

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
    fun getCardex(lineamiento: Int) {
        viewModelScope.launch {
            _cardexState.value = CardexUiState.Loading
            val result = repository.getCardex(lineamiento)
            if (result.isNotEmpty()) {
                _cardexState.value = CardexUiState.Success(result)
            } else {
                _cardexState.value = CardexUiState.Error("No se encontraron datos o hubo un error")
            }
        }
    }

    fun getCalificacionesFinales(modEducativo: Int) {
        viewModelScope.launch {
            _califFinalState.value = CalifFinalUiState.Loading
            val result = repository.getCalificacionesFinales(modEducativo)
            _califFinalState.value = CalifFinalUiState.Success(result)
        }
    }

    fun getCalificacionesUnidad() {
        viewModelScope.launch {
            _califUnidadState.value = CalifUnidadUiState.Loading
            val result = repository.getCalificacionesUnidad()
            _califUnidadState.value = CalifUnidadUiState.Success(result)
        }
    }
    
    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
        _profileState.value = ProfileUiState.Loading
        _califFinalState.value = CalifFinalUiState.Loading
        _califUnidadState.value = CalifUnidadUiState.Loading
        repository.clearSession()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as SicenetApplication)
                val sicenetRepository = application.container.sicenetRepository
                SicenetViewModel(repository = sicenetRepository)
            }
        }
    }
}
