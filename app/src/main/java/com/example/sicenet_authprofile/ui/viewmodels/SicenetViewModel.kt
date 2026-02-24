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
import com.example.sicenet_authprofile.data.model.Materia
import com.example.sicenet_authprofile.data.model.PerfilAcademico
import com.example.sicenet_authprofile.data.repository.SicenetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val cookie: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}


class SicenetViewModel(
    private val repository: SicenetRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()


    val profileState = repository.getProfileFromDb()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val cargaState = repository.getCargaAcademicaFromDb()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val cardexState = repository.getCardexFromDb()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val califFinalState = repository.getCalificacionesFinalesFromDb()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val califUnidadState = repository.getCalificacionesUnidadFromDb()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Observar el perfil para disparar sincronizaciones que dependen de él (Cardex y Calif Finales)
        // una vez que el perfil esté disponible en la DB local.
        viewModelScope.launch {
            profileState.filterNotNull().collectLatest { perfil ->
                // Cuando el perfil se cargue/actualice en la DB, disparamos las sincronizaciones dependientes
                // si es que no tienen datos o queremos asegurar que estén frescas.
                repository.sincronizarDato("CARDEX", perfil.lineamiento, 0)
                repository.sincronizarDato("CALIF_FINAL", 0, perfil.modEducativo)
            }
        }
    }

    fun login(user: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            val result = repository.login(user, pass)
            if (result.success && result.cookie != null) {
                _loginState.value = LoginUiState.Success(result.cookie)
                // Iniciamos la sincronización de todo lo que no depende de parámetros del perfil
                sincronizarDatosIniciales()
            } else {
                _loginState.value = LoginUiState.Error(result.message ?: "Error desconocido")
            }
        }
    }

    private fun sincronizarDatosIniciales() {
        // Estas peticiones no necesitan lineamiento ni modEducativo (o usan valores por defecto internos)
        sincronizarPerfil()
        sincronizarCargaAcademica()
        sincronizarCalificacionesUnidad()
    }

    fun sincronizarPerfil(){
        repository.sincronizarDato(tipoSync = "PERFIL", lineamiento = 0, modEducativo = 0)
    }

    fun sincronizarCargaAcademica() {
        repository.sincronizarDato("CARGA_ACADEMICA", 0, 0)
    }

    fun sincronizarCardex(){
        val perfilActual = profileState.value
        if (perfilActual != null) {
            val lineamiento = perfilActual.lineamiento
            repository.sincronizarDato("CARDEX", lineamiento, 0)
        }
    }

    fun sincronizarCalificacionesUnidad() {
        repository.sincronizarDato("CALIF_UNIDAD", 0, 0)
    }

    fun sincronizarCalificacionesFinales() {
        val perfilActual = profileState.value
        if (perfilActual != null) {
            val modEducativo = perfilActual.modEducativo
            repository.sincronizarDato("CALIF_FINAL", 0, modEducativo)
        }
    }


    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
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
