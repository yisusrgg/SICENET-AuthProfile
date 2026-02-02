package com.example.sicenet_authprofile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sicenet_authprofile.ui.screens.LoginScreen
import com.example.sicenet_authprofile.ui.screens.ProfileScreen
import com.example.sicenet_authprofile.ui.theme.SICENETAuthProfileTheme
import com.example.sicenet_authprofile.ui.viewmodels.LoginUiState
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SICENETAuthProfileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SicenetApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SicenetApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModel: SicenetViewModel = viewModel(factory = SicenetViewModel.Factory)
    val loginState by viewModel.loginState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { cookie ->
                    navController.navigate("profile/$cookie") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("profile/{cookie}") { backStackEntry ->
            val cookie = backStackEntry.arguments?.getString("cookie") ?: ""
            ProfileScreen(
                viewModel = viewModel,
                cookie = cookie,
                onLogout = {
                    viewModel.resetLoginState()
                    navController.navigate("login") {
                        popUpTo("profile/{cookie}") { inclusive = true }
                    }
                }
            )
        }
    }
}
