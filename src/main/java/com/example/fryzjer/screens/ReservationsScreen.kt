package com.example.fryzjer.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontWeight
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
    var reasons by remember { mutableStateOf<Map<String, String?>>(emptyMap()) } // Hold reasons by reservation ID
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch reservations when screen is first displayed or when user changes
    LaunchedEffect(Unit) {
        try {
            Log.d("ReservationsScreen", "Starting to fetch reservations...")

            val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            Log.d("ReservationsScreen", "User retrieved: ${user.id}")

            val result = ReservationRepository.getReservationsByUserId(user.id)
            Log.d("ReservationsScreen", "Fetch result: $result")

            if (result != null && result.data != null) {
                try {
                    reservations = Json.decodeFromString(result.data as String)
                    Log.d("ReservationsScreen", "Reservations data fetched: ${reservations.size} items")

                    // Fetch reasons for each reservation
                    val reasonsMap = mutableMapOf<String, String?>()
                    reservations.forEach { reservation ->
                        val reason = ReservationRepository.getReasonForReservation(reservation.reservation_id)
                        reasonsMap[reservation.reservation_id] = reason
                    }
                    reasons = reasonsMap
                } catch (e: Exception) {
                    error = "Error parsing reservations data: ${e.message}"
                    Log.e("ReservationsScreen", "Error parsing reservations", e)
                }
            } else {
                error = "Failed to load reservations: No data available"
                Log.e("ReservationsScreen", error ?: "Unknown error")
            }
        } catch (e: Exception) {
            error = e.message ?: "An error occurred"
            Log.e("ReservationsScreen", "Error fetching reservations", e)
        } finally {
            loading = false
            Log.d("ReservationsScreen", "Loading state updated to false.")
        }
    }

    // Display the common screen with the reservations content
    CommonScreen(
        navController = navController,
        title = "Rezerwacje",
        bodyText = "",
        logoutAction = null,
        content = { paddingValues ->
            ReservationsContent(
                paddingValues = paddingValues,
                reservations = reservations,
                reasons = reasons,
                loading = loading,
                error = error
            )
        }
    )
}

@Composable
fun ReservationsContent(
    paddingValues: PaddingValues,
    reservations: List<ReservationInput>,
    reasons: Map<String, String?>,
    loading: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Twoje rezerwacje",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else if (error != null) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else if (reservations.isEmpty()) {
            Text(
                text = "Nie masz żadnych rezerwacji",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Add headers for the table
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reservations) { reservation ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = reservation.date,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = reservation.time ?: "Unknown",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = reservation.description ?: "No description",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(2f)
                            )
                            // Replace the status text with appropriate icons
                            when (reservation.is_accepted) {
                                true -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Accepted",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                false -> {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Rejected",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                else -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(end = 4.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Pending",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }

                        // Display the reason below the reservation details
                        val reason = reasons[reservation.reservation_id]
                        if (!reason.isNullOrEmpty()) {
                            Text(
                                text = "Powód odrzucenia: $reason,prosimy złożyć rezerwacje ponownie.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}















