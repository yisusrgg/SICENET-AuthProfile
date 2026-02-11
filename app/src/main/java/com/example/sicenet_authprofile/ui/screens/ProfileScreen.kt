package com.example.sicenet_authprofile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
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
                            ProfileItem("Nombre", state.profile.nombre)
                            ProfileItem("Matrícula", state.profile.matricula)
                            ProfileItem("Carrera", state.profile.carrera)
                            ProfileItem("Especialidad", state.profile.especialidad)
                            ProfileItem("Semestre actual", state.profile.semActual.toString())
                            ProfileItem(
                                "Créditos acumulados",
                                state.profile.cdtosAcumulados.toString()
                            )
                            ProfileItem("Créditos actuales", state.profile.cdtosActuales.toString())
                            ProfileItem("Estatus", state.profile.estatus)
                            ProfileItem("Inscrito", if (state.profile.inscrito) "Sí" else "No")
                            ProfileItem("Adeudo", if (state.profile.adeudo) "Sí" else "No")
                            ProfileItem("Fecha reinscripción", state.profile.fechaReins)
                            ProfileItem(
                                "Modalidad educativa",
                                state.profile.modEducativo.toString()
                            )
                            ProfileItem("Lineamiento", state.profile.lineamiento.toString())
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
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}
