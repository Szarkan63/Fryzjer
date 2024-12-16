package com.example.fryzjer.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fryzjer.SupabaseAuthViewModel
import com.example.fryzjer.data.model.UserState

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: SupabaseAuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val userState by viewModel.userState

    LaunchedEffect(Unit) {
        viewModel.isUserLoggedIn(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login to Fryzjer",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LoginForm(
            userEmail = userEmail,
            onEmailChange = { userEmail = it },
            userPassword = userPassword,
            onPasswordChange = { userPassword = it },
            onLoginClick = { viewModel.login(context, userEmail, userPassword) },
            onSignUpClick = { navController.navigate("register") }
        )

        when (userState) {
            is UserState.Loading -> {
                CircularProgressIndicator()
            }
            is UserState.Success -> {
                val successState = userState as UserState.Success
                if (!successState.isRegistration) {
                    LaunchedEffect(Unit) {
                        navController.navigate("home") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            }
            is UserState.Error -> {
                errorMessage = (userState as UserState.Error).message
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun LoginForm(
    userEmail: String,
    onEmailChange: (String) -> Unit,
    userPassword: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = userEmail,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(0.8f),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = userPassword,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(0.8f),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(0.8f).height(48.dp)
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth(0.8f).height(48.dp)
        ) {
            Text("Sign Up")
        }
    }
}
