package com.example.sicenet_authprofile.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("informacion") },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Info") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("cardex") },
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text("Cardex") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("calificaciones") },
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            label = { Text("Calif." ) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("final") },
            icon = { Icon(Icons.Default.Check, contentDescription = null) },
            label = { Text("Final") }
        )
    }
}
