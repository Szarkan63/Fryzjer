package com.example.fryzjer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fryzjer.SupabaseAuthViewModel

@Composable
fun HomeScreen(
    viewModel: SupabaseAuthViewModel = viewModel(), // ViewModel do wylogowania
    navController: NavController // NavController do nawigacji
) {
    // Pobieranie kontekstu w kompozycyjny sposób
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Welcome to the Home Screen")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.logout(context) // Użycie kontekstu zdefiniowanego wcześniej
                navController.navigate("main") {
                    popUpTo("home") { inclusive = true } // Usuwa ekran "home" ze stosu
                }
            }
        ) {
            Text("Logout")
        }
    }
}
