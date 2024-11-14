package com.example.fryzjer.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fryzjer.data.model.UserState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fryzjer.LoadingComponent
import com.example.fryzjer.SupabaseAuthViewModel


@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: SupabaseAuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userState by viewModel.userState

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var userFirstName by remember { mutableStateOf("") }
    var userLastName by remember { mutableStateOf("") }

    var currentUserState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.isUserLoggedIn(context)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(8.dp)
    ) {
        TextField(
            value = userEmail,
            onValueChange = { userEmail = it },
            placeholder = { Text("Enter email") }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        TextField(
            value = userPassword,
            onValueChange = { userPassword = it },
            placeholder = { Text("Enter Password") }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        TextField(
            value = userFirstName,
            onValueChange = { userFirstName = it },
            placeholder = { Text("Enter First Name") }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        TextField(
            value = userLastName,
            onValueChange = { userLastName = it },
            placeholder = { Text("Enter Last Name") }
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            onClick = {
                viewModel.signUp(context, userEmail, userPassword, userFirstName, userLastName)
            }
        ) {
            Text("Sign Up")
        }

        Button(
            onClick = {
                navController.popBackStack() // Navigate back to previous screen (MainScreen)
            }
        ) {
            Text("Back to Login")
        }

        when (userState) {
            is UserState.Loading -> {
                LoadingComponent()
            }
            is UserState.Success -> {
                val successState = userState as UserState.Success
                currentUserState = successState.message
                if (!successState.isRegistration) {
                    LaunchedEffect(Unit) {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                }
            }
            is UserState.Error -> {
                val message = (userState as UserState.Error).message
                currentUserState = message
            }
        }

        if (currentUserState.isNotEmpty()) {
            Text(currentUserState)
        }
    }
}

