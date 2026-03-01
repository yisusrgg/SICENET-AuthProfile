package com.example.sicenet_authprofile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sicenet_authprofile.ui.screens.HomeScreen
import com.example.sicenet_authprofile.ui.screens.LoginScreen
import com.example.sicenet_authprofile.ui.theme.SICENETAuthProfileTheme
import com.example.sicenet_authprofile.ui.viewmodels.SicenetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModel: SicenetViewModel = viewModel(factory = SicenetViewModel.Factory)
    val startDestination = remember {
        val sharedPrefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val savedCookies = sharedPrefs.getStringSet("PREF_COOKIES", emptySet())
        // Si el set de cookies no está vacío,no necesita login
        val needLogin = savedCookies?.isNotEmpty() == false
        if (!needLogin) {
            "home"
        } else {
            "login"
        }
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.resetLoginState()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

    }
}
