package com.example.fryzjer.screens
import androidx.compose.ui.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fryzjer.SupabaseAuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fryzjer.data.model.ReservationRepository
import com.example.fryzjer.data.model.ReservationState
import com.example.fryzjer.data.network.SupabaseClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MakeReservationScreen(
    navController: NavController,
    viewModel: SupabaseAuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State variables for user input
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var reservationState by remember { mutableStateOf<ReservationState>(ReservationState.Loading) }

    CommonScreen(
        navController = navController,
        title = "Złóż rezerwację",
        bodyText = "Here you can make a new reservation.",
        logoutAction = { viewModel.logout(context) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Zarezerwuj termin",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description Input Field
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker
                DatePickerField(
                    selectedDate = selectedDate,
                    onDateChange = { date -> selectedDate = date }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Time Picker
                TimePickerField(
                    selectedTime = selectedTime,
                    onTimeChange = { time -> selectedTime = time }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(onClick = {
                    val isDateValid = validateDate(selectedDate, selectedTime)

                    if (!isDateValid.first) {
                        reservationState = ReservationState.Error(isDateValid.second)
                        return@Button
                    }

                    coroutineScope.launch {
                        reservationState = ReservationState.Loading
                        try {
                            val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
                            val userId = user.id
                            val isAccepted = null

                            // Generate a unique reservation ID
                            val reservationId = UUID.randomUUID().toString()

                            // Call the createReservation function
                            ReservationRepository.createReservation(
                                reservation_id = reservationId,
                                date = selectedDate.toString(),
                                description = description,
                                time = selectedTime.toString(),
                                is_accepted = isAccepted,
                                user_id = userId
                            )

                            // After successful reservation, reset the state
                            reservationState = ReservationState.Success("Rezerwacja została pomyślnie złożona!")
                            description = ""  // Clear description
                            selectedDate = LocalDate.now()  // Reset to current date
                            selectedTime = LocalTime.now()  // Reset to current time

                        } catch (e: Exception) {
                            reservationState = ReservationState.Error("Wystąpił błąd: ${e.message}")
                        }
                    }
                }) {
                    Text("Zarezerwuj")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display reservation state message
                when (val state = reservationState) {
                    is ReservationState.Loading -> {
                        // CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    }
                    is ReservationState.Success -> {
                        Text(
                            text = state.message,
                            color = Color.Green, // Change this to MaterialTheme.colorScheme.success or any green color
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is ReservationState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}




@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(selectedDate: LocalDate, onDateChange: (LocalDate) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val state = rememberDatePickerState()

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val epochMillis = state.selectedDateMillis
                    if (epochMillis != null) {
                        val newDate = LocalDate.ofEpochDay(epochMillis / (24 * 60 * 60 * 1000))
                        onDateChange(newDate)
                    }
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = state)
        }
    }

    OutlinedButton(
        onClick = { showDialog = true },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Text("Wybierz datę: $selectedDate")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(selectedTime: LocalTime, onTimeChange: (LocalTime) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDialog) {
        TimePickerDialog(
            onDismissRequest = { showDialog = false },
            onTimeConfirm = { hour, minute ->
                onTimeChange(LocalTime.of(hour, minute)) // Update the selected time
                showDialog = false
            }
        )
    }

    // Display selected time in a user-friendly format, e.g., "HH:mm"
    OutlinedButton(onClick = { showDialog = true },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White) ) {
        Text("Wybierz godzinę: ${selectedTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onTimeConfirm(state.hour, state.minute)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = state)
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun validateDate(selectedDate: LocalDate, selectedTime: LocalTime): Pair<Boolean, String> {
    val currentDate = LocalDate.now()
    val currentTime = LocalTime.now()

    // Check if the selected date is in the past
    if (selectedDate.isBefore(currentDate)) {
        return Pair(false, "Nie można rezerwować terminów z przeszłości.")
    }

    // If the selected date is today, ensure the time is in the future
    if (selectedDate == currentDate && selectedTime.isBefore(currentTime)) {
        return Pair(false, "Nie można rezerwować godzin z przeszłości.")
    }

    val dayOfWeek = selectedDate.dayOfWeek
    val openingTime: LocalTime
    val closingTime: LocalTime

    // Check operating hours based on the day of the week
    when (dayOfWeek) {
        java.time.DayOfWeek.SUNDAY -> return Pair(false, "Lokal jest zamknięty w niedziele.")
        java.time.DayOfWeek.SATURDAY -> {
            openingTime = LocalTime.of(8, 0)
            closingTime = LocalTime.of(14, 0)
        }
        else -> { // Monday to Friday
            openingTime = LocalTime.of(8, 0)
            closingTime = LocalTime.of(17, 0)
        }
    }

    // Check if the selected time is within operating hours
    return if (selectedTime.isBefore(openingTime) || selectedTime.isAfter(closingTime)) {
        Pair(false, "Lokal jest otwarty od $openingTime do $closingTime.")
    } else {
        Pair(true, "")
    }
}













