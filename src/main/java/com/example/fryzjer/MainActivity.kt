package com.example.fryzjer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fryzjer.data.model.UserState
import com.example.fryzjer.ui.theme.FryzjerTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fryzjer.ui.HomeScreen

class MainActivity : ComponentActivity() {
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
                    }
                }
            }
        }
    }

    @Composable
    fun MainScreen(
        navController: NavController,
        viewModel: SupabaseAuthViewModel = viewModel(),
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        val userState by viewModel.userState

        var userEmail by remember { mutableStateOf("") }
        var userPassword by remember { mutableStateOf("") }

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
            Button(
                onClick = {
                    viewModel.signUp(context, userEmail, userPassword)
                }
            ) {
                Text("Sign Up")
            }
            Button(
                onClick = {
                    viewModel.login(context, userEmail, userPassword)
                }
            ) {
                Text("Login")
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                onClick = {
                    viewModel.logout(context)
                }
            ) {
                Text("Logout")
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
                                popUpTo("main") { inclusive = true }
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
}



