package com.example.fryzjer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fryzjer.SupabaseAuthViewModel
import com.example.fryzjer.data.network.SupabaseClient
import io.github.jan.supabase.auth.auth

@Composable
fun HomeScreen(
    viewModel: SupabaseAuthViewModel = viewModel(), // ViewModel do wylogowania
    navController: NavController
) {
    // Pobieranie kontekstu w kompozycyjny sposób
    val context = LocalContext.current

    // Pamiętanie stanu użytkownika
    val userId = remember { mutableStateOf<String?>(null) }

    // Using LaunchedEffect to call suspend function safely within a Composable
    LaunchedEffect(Unit) {
        // Fetch user session asynchronously
        val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)

        // Sprawdzamy, czy użytkownik jest zalogowany
        userId.value = user.id
    }

    // Layout aplikacji
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Welcome to the Home Screen")

        Spacer(modifier = Modifier.height(16.dp))

        // Wyświetlenie ID użytkownika
        Text(text = "User ID: ${userId.value}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Logout i nawigacja
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

