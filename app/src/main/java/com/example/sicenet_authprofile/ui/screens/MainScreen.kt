package com.example.sicenet_authprofile.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sicenet_authprofile.ui.components.BottomBar
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel

@Composable
fun MainScreen(navController: NavHostController, cookie: String, viewModel: SicenetViewModel) {
    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "informacion",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("informacion") { ProfileScreen(viewModel, cookie, onLogout = {
                navController.navigate("login") { popUpTo(0) }
            }) }
            composable("cardex") { CardexScreen(viewModel) }
            composable("calificaciones") { CalificacionesUnidadScreen() }
            composable("final") { CalificacionFinalScreen() }
        }
    }
}
