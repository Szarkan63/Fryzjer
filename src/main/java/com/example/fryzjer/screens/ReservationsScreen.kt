package com.example.fryzjer.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fryzjer.SupabaseAuthViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.fryzjer.data.model.ReservationInput
import com.example.fryzjer.data.model.ReservationRepository
import com.example.fryzjer.data.network.SupabaseClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Composable
fun ReservationsScreen(
    navController: NavController,
    viewModel: SupabaseAuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // State to hold reservations data
    var reservations by remember { mutableStateOf<List<ReservationInput>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }  // Make sure error is mutable

    // Fetch reservations when screen is first displayed or when user changes
    LaunchedEffect(Unit) { // LaunchedEffect is already a coroutine scope
        try {
            Log.d("ReservationsScreen", "Starting to fetch reservations...")

            // Make the suspend function call to fetch the reservations
            val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            Log.d("ReservationsScreen", "User retrieved: ${user.id}")

            val result = ReservationRepository.getReservationsByUserId(user.id)
            Log.d("ReservationsScreen", "Fetch result: $result")

            // Log the result to see the raw data structure
            Log.d("ReservationsScreen", "Raw result data: ${result.data}")

            // Try to decode the data properly based on its structure
            // If the result is a JSON string, you can parse it into the desired type
            if (result != null && result.data != null) {
                // If the data is a JSON string, parse it
                try {
                    reservations = Json.decodeFromString<List<ReservationInput>>(result.data as String)
                    Log.d("ReservationsScreen", "Reservations data fetched: ${reservations.size} items")
                } catch (e: Exception) {
                    error = "Error parsing reservations data: ${e.message}"
                    Log.e("ReservationsScreen", "Error parsing reservations", e)
                }
            } else {
                // Handle the case where result is null or data is null
                error = "Failed to load reservations: No data available"
                Log.e("ReservationsScreen", error ?: "Unknown error")
            }
        } catch (e: Exception) {
            // Catch any exceptions and show an error message
            error = e.message ?: "An error occurred"
            Log.e("ReservationsScreen", "Error fetching reservations", e)
        } finally {
            // Ensure loading state is updated after the data fetch
            loading = false
            Log.d("ReservationsScreen", "Loading state updated to false.")
        }
    }

    // Display the common screen with the reservations content
    CommonScreen(
        navController = navController,
        title = "Rezerwacje",
        bodyText = "",
        logoutAction = { viewModel.logout(context) },
        content = { paddingValues ->
            ReservationsContent(
                paddingValues = paddingValues,
                reservations = reservations,
                loading = loading,
                error = error // Pass the error state to the ReservationsContent composable
            )
        }
    )
}

@Composable
fun ReservationsContent(
    paddingValues: PaddingValues,
    reservations: List<ReservationInput>,
    loading: Boolean,
    error: String? // Receive error as a parameter
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Upcoming Reservations",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp
        )

        // Show loading indicator while fetching data
        if (loading) {
            CircularProgressIndicator()
        } else if (error != null) {
            // Display an error message if the fetch failed
            Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
            Log.d("ReservationsContent", "Error displayed: $error")
        } else if (reservations.isEmpty()) {
            // Inform the user if there are no upcoming reservations
            Text(text = "No upcoming reservations")
            Log.d("ReservationsContent", "No upcoming reservations.")
        } else {
            // Display the list of reservations
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reservations.forEach { reservation ->
                    Text(
                        text = "${reservation.date} at ${reservation.time ?: "Unknown time"}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp
                    )
                    Log.d("ReservationsContent", "Displayed reservation: ${reservation.date} at ${reservation.time}")
                }
            }
        }
    }
}








