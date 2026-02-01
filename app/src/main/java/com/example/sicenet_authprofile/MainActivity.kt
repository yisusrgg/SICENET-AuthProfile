package com.example.sicenet_authprofile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sicenet_authprofile.ui.screens.LoginScreen
import com.example.sicenet_authprofile.ui.screens.ProfileScreen
import com.example.sicenet_authprofile.ui.theme.SICENETAuthProfileTheme
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SICENETAuthProfileTheme {
                val navController = rememberNavController()
                val sicenetViewModel: SicenetViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = sicenetViewModel,
                                onLoginSuccess = { cookie ->
                                    // Encoding cookie if it contains special characters for the route
                                    val encodedCookie = URLEncoder.encode(cookie, StandardCharsets.UTF_8.toString())
                                    navController.navigate("profile/$encodedCookie")
                                }
                            )
                        }
                        composable(
                            route = "profile/{cookie}",
                            arguments = listOf(navArgument("cookie") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val cookie = backStackEntry.arguments?.getString("cookie") ?: ""
                            ProfileScreen(
                                viewModel = sicenetViewModel,
                                cookie = cookie,
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
