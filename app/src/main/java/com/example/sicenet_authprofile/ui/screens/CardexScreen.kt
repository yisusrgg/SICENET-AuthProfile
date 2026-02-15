package com.example.sicenet_authprofile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sicenet_authprofile.ui.viewmodels.CardexUiState
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel
import com.example.sicenet_authprofile.ui.viewmodels.ProfileUiState

@Composable
fun CardexScreen(viewModel: SicenetViewModel) {
    val cardexState by viewModel.cardexState.collectAsState()
    val profileState by viewModel.profileState.collectAsState()

    LaunchedEffect(Unit) {
        val lineamiento = if (profileState is ProfileUiState.Success) {
            (profileState as ProfileUiState.Success).profile.lineamiento.toString()
        } else {
            "3" // Por defecto 3 suele funcionar mejor para créditos
        }
        viewModel.getCardex(lineamiento)
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (val state = cardexState) {
            is CardexUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is CardexUiState.Success -> {
                Column {
                    Text(
                        "Kárdex Académico",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.materias) { materia ->
                            Card(
                                elevation = CardDefaults.cardElevation(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = materia.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Calif: ${materia.calificacion}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Sem: ${materia.semestre}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text(
                                        text = "Clave: ${materia.clave}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is CardexUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = { viewModel.getCardex("3") },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}