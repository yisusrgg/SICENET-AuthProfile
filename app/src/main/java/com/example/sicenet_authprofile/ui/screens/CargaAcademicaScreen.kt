package com.example.sicenet_authprofile.ui.screens

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenet_authprofile.data.model.Materia
import com.example.sicenet_authprofile.ui.viewmodels.CargaUiState
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargaAcademicaScreen(
    viewModel: SicenetViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.cargaState.collectAsState()
    val sicenetBlue = MaterialTheme.colorScheme.primary

    // Estado para controlar qué día está seleccionado (0=Lun, 1=Mar, etc.)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val diasSemana = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO")

    LaunchedEffect(Unit) {
        viewModel.getCargaAcademica()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(color = sicenetBlue, contentColor = Color.White) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mi Horario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

        when (val carga = state) {
            is CargaUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = sicenetBlue)
                }
            }
            is CargaUiState.Success -> {
                val materias = carga.list ?: emptyList()

                // --- EL "CINTURÓN" (TABS DE DÍAS) ---
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = sicenetBlue,
                    contentColor = Color.White,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color.White
                        )
                    }
                ) {
                    diasSemana.forEachIndexed { index, dia ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(dia, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                // Filtrar materias según el día seleccionado
                val materiasDelDia = filtrarMateriasPorDia(materias, selectedTabIndex)

                if (materiasDelDia.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tienes clases este día.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(materiasDelDia) { item ->
                            HorarioItem(item.first, item.second) // materia y el horario específico
                        }
                    }
                }
            }
            is CargaUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${carga.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun HorarioItem(materia: Materia, horario: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna de la Hora
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = horario.split(" ").firstOrNull() ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Hrs", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))
            VerticalDivider(modifier = Modifier.height(40.dp), thickness = 2.dp, color = MaterialTheme.colorScheme.primaryContainer)
            Spacer(modifier = Modifier.width(16.dp))

            // Info de la Materia
            Column {
                Text(
                    text = materia.materia,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(
                        text = " Aula: ${horario.substringAfter("Aula: ", "N/A")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = materia.docente,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

            }
        }
    }
}

// Función lógica para filtrar
fun filtrarMateriasPorDia(materias: List<Materia>, diaIndex: Int): List<Pair<Materia, String>> {
    return materias.mapNotNull { materia ->
        val horario = when (diaIndex) {
            0 -> materia.lunes
            1 -> materia.martes
            2 -> materia.miercoles
            3 -> materia.jueves
            4 -> materia.viernes
            5 -> materia.sabado
            else -> ""
        }
        if (horario.isNotBlank() && horario != "null") materia to horario else null
    }.sortedBy { it.second } // Ordenar por hora
}