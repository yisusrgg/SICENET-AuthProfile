package com.example.sicenet_authprofile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel

@Composable
fun HomeScreen(
    cookie: String,
    viewModel: SicenetViewModel,
    onLogout: () -> Unit
) {

    val navController = rememberNavController()

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            val currentRoute =
                navController.currentBackStackEntryAsState().value?.destination?.route

            fun navigate(route: String) {
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }

            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    @Composable
                    fun NavItem(
                        route: String,
                        icon: ImageVector,
                        label: String
                    ) {
                        val selected = currentRoute == route

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navigate(route) }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp),
                                tint = if (selected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    NavItem("informacion", Icons.Default.AccountCircle, "Info")
                    NavItem("cardex", Icons.Default.Menu, "Cardex")
                    NavItem("unidad", Icons.Default.List, "Unidad")
                    NavItem("final", Icons.Default.Star, "Final")
                }
            }
        }


    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "informacion",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("informacion") {
                ProfileScreen(
                    viewModel = viewModel,
                    cookie = cookie,
                    onLogout = onLogout
                )
            }

            composable("cardex") {
                CardexScreen()
            }

            composable("unidad") {
                CalificacionesUnidadScreen()
            }

            composable("final") {
                CalificacionFinalScreen()
            }
        }
    }
}
