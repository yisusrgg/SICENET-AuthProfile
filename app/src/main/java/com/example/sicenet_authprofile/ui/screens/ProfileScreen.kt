package com.example.sicenet_authprofile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sicenet_authprofile.data.model.UserProfile
import com.example.sicenet_authprofile.ui.viewmodels.ProfileUiState
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel

@Composable
fun ProfileScreen(
    viewModel: SicenetViewModel,
    cookie: String,
    onLogout: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getProfile(cookie)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Perfil Académico", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        when (val state = profileState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator()
            }
            is ProfileUiState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileItem(label = "Nombre", value = state.profile.nombre)
                        ProfileItem(label = "Matrícula", value = state.profile.matricula)
                        ProfileItem(label = "Carrera", value = state.profile.carrera)
                        ProfileItem(label = "Situación", value = state.profile.situacion)
                        ProfileItem(label = "Promedio", value = state.profile.promedio)
                    }
                }
            }
            is ProfileUiState.Error -> {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onLogout) {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}
