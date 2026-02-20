package com.example.sicenet_authprofile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenet_authprofile.data.model.CalificacionUnidad
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel

@Composable
fun CalificacionesUnidadScreen(
    viewModel: SicenetViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.califUnidadState.collectAsState()
    val sicenetBlue = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        viewModel.sincronizarCalificacionesUnidad()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header Verde con botón de Salir
        Surface(
            color = sicenetBlue,
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
                    text = "Calificaciones Parciales",
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

        if(state.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = sicenetBlue)
            }
        }else{
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state) { calif ->
                    SubjectAccordion(calif, sicenetBlue)
                }
            }
        }
    }
}

@Composable
fun SubjectAccordion(calif: CalificacionUnidad, accentColor: Color) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = accentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = calif.materia.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    calif.unidades.forEachIndexed { index, score ->
                        UnitRow("Unidad ${index + 1}", score, accentColor)
                    }
                }
            }
        }
    }
}

@Composable
fun UnitRow(label: String, score: String, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        
        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.LightGray))

        val scoreValue = score.toIntOrNull() ?: 0
        val statusColor = if (scoreValue >= 70) Color(0xFF378E3D) else Color(0xFFD32F2F)
        
        Box(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .background(statusColor.copy(alpha = 0.1f))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = score,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
}
