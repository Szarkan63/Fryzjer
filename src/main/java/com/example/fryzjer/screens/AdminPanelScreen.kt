package com.example.fryzjer.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.fryzjer.data.model.ReservationInput
import com.example.fryzjer.data.model.ReservationRepository
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun AdminPanelScreen(
    navController: NavController
) {
    var reservations by remember { mutableStateOf<List<ReservationInput>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch reservations when the screen loads
    LaunchedEffect(Unit) {
        try {
            Log.d("AdminPanelScreen", "Starting to fetch reservations...")
            val result = ReservationRepository.getAllReservations()
            if (result != null && result.data != null) {
                reservations = Json.decodeFromString(result.data as String)
                Log.d("AdminPanelScreen", "Fetched ${reservations.size} reservations")
            } else {
                error = "Failed to load reservations: No data available"
            }
        } catch (e: Exception) {
            error = e.message ?: "An error occurred"
            Log.e("AdminPanelScreen", "Error fetching reservations", e)
        } finally {
            loading = false
        }
    }

    CommonScreen(
        navController = navController,
        title = "Admin Panel",
        bodyText = "",
        content = { paddingValues ->
            AdminPanelContent(
                paddingValues = paddingValues,
                reservations = reservations,
                loading = loading,
                error = error
            )
        }
    )
}

@Composable
fun AdminPanelContent(
    paddingValues: PaddingValues,
    reservations: List<ReservationInput>,
    loading: Boolean,
    error: String?
) {
    var selectedReservation by remember { mutableStateOf<ReservationInput?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Admin Panel - Reservations",
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
                text = "No reservations found.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
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
                }

                items(reservations) { reservation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedReservation = reservation },
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
                        if (reservation.is_accepted == true) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Accepted",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        } else if (reservation.is_accepted == false) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Rejected",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
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
            }
        }

        if (selectedReservation != null) {
            AdminDecisionDialog(
                reservation = selectedReservation!!,
                onClose = { selectedReservation = null },
                onUpdate = { reservationId, isAccepted ->
                    try {
                        ReservationRepository.updateReservationStatus(
                            reservationId = reservationId,
                            isAccepted = isAccepted
                        )
                    } catch (e: Exception) {
                        Log.e("AdminPanelScreen", "Error updating reservation: $e")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminDecisionDialog(
    reservation: ReservationInput,
    onClose: () -> Unit,
    onUpdate: suspend (String, Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope() // Get a coroutine scope for handling suspend functions

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(text = "Update Reservation Status")
        },
        text = {
            Text("Would you like to accept or reject this reservation?")
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        onUpdate(reservation.reservation_id, true) // Accept reservation
                        onClose() // Close dialog after update
                    }
                }
            ) {
                Text("Accept")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        onUpdate(reservation.reservation_id, false) // Reject reservation
                        onClose() // Close dialog after update
                    }
                }
            ) {
                Text("Reject")
            }
        }
    )
}









