package com.example.sicenet_authprofile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel

@Composable
fun ProfileScreen(
    viewModel: SicenetViewModel,
    cookie: String,
    onLogout: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()
    //val sharedPrefs = context.getSharedPreferences("SICENET_PREFS", Context.MODE_PRIVATE)
    //val ultimaFecha = sharedPrefs.getString("FECHA_ACT_PERFIL", "Sin sincronizar") ?: "Sin sincronizar"

    LaunchedEffect(Unit) {
        viewModel.sincronizarPerfil()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header Verde con botón de Salir
        Surface(
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perfil Académico",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Salir",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SALIR", fontWeight = FontWeight.Bold)
                }
            }
        }

        /*Text(
            text = "Última actualización: $ultimaFecha",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, top = 8.dp)
        )*/

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if(profileState == null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }else{
                profileState?.let { profile ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ProfileItem("Nombre", profile.nombre)
                            ProfileItem("Matrícula", profile.matricula)
                            ProfileItem("Carrera", profile.carrera)
                            ProfileItem("Especialidad", profile.especialidad)
                            ProfileItem("Semestre actual", profile.semActual.toString())
                            ProfileItem(
                                "Créditos acumulados",
                                profile.cdtosAcumulados.toString()
                            )
                            ProfileItem("Créditos actuales", profile.cdtosActuales.toString())
                            ProfileItem("Estatus", profile.estatus)
                            ProfileItem("Inscrito", if (profile.inscrito) "Sí" else "No")
                            ProfileItem("Adeudo", if (profile.adeudo) "Sí" else "No")
                            ProfileItem("Fecha reinscripción", profile.fechaReins)
                            ProfileItem(
                                "Modalidad educativa",
                                profile.modEducativo.toString()
                            )
                            ProfileItem("Lineamiento", profile.lineamiento.toString())
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium, 
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color.LightGray.copy(alpha = 0.5f))
    }
}
