package com.example.fryzjer

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fryzjer.screens.HomeScreen
import com.example.fryzjer.screens.MainScreen
import com.example.fryzjer.screens.MakeReservationScreen
import com.example.fryzjer.screens.ReservationsScreen
import com.example.fryzjer.ui.RegisterScreen
import com.example.fryzjer.ui.theme.FryzjerTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            FryzjerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(navController = navController)
                        }
                        composable("home") {
                            HomeScreen(navController = navController)
                        }
                        composable("register") {
                            RegisterScreen(navController = navController)
                        }
                        composable("makeReservation") {
                            MakeReservationScreen(navController = navController)
                        }
                        composable("Reservations") {
                            ReservationsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}













