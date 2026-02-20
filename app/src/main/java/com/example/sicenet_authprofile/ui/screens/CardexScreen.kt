package com.example.sicenet_authprofile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel

@Composable
fun CardexScreen(
    viewModel: SicenetViewModel,
    onLogout: () -> Unit
) {
    val cardexState by viewModel.cardexState.collectAsState()
    //val profileState by viewModel.profileState.collectAsState()
    val sicenetBlue = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        viewModel.sincronizarCardex()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        //encabezado
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
                    text = "Kardex General",
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

        //content
        if(cardexState.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = sicenetBlue)
            }
        }
        else{
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cardexState) { item ->
                    CardexItemRow(item, sicenetBlue)
                }
            }
        }
    }
}

@Composable
fun CardexItemRow(item: com.example.sicenet_authprofile.data.model.CardexItem, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Semestre ${item.semestre}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = item.materia,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Créditos: ${item.creditos}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = if ((item.calificacion.toIntOrNull() ?: 0) >= 85)
                            Color(0xFFE8F5E9)
                        else if ((item.calificacion.toIntOrNull() ?: 0) in 70..<85)
                            Color(0xFFFFF59D)
                        else
                            Color(0xFFFFEBEE),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = item.calificacion,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if ((item.calificacion.toIntOrNull() ?: 0) >= 85)
                                Color(0xFF2E7D32)
                            else if ((item.calificacion.toIntOrNull() ?: 0) in 70..<85)
                                Color(0xFFF57F17)
                            else
                                Color(0xFFC62828)
                )
            }
        }
    }
}