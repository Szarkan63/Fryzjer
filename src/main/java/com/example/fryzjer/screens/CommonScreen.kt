package com.example.fryzjer.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fryzjer.data.model.UserViewModel
import com.example.fryzjer.data.network.SupabaseClient
import kotlinx.coroutines.launch


@Composable
fun CommonScreen(
    navController: NavController,
    title: String,
    bodyText: String,
    logoutAction: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userViewModel: UserViewModel = viewModel()

    LaunchedEffect(Unit) {
        val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
        userViewModel.setUserId(user.id)
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(navController = navController, userId = userViewModel.userId.value) // Przekazujemy userId
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title,
                    onOpenDrawer = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = bodyText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 20.sp
                    )
                }

                logoutAction?.let {
                    Button(
                        onClick = {
                            it()
                            navController.navigate("main") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 50.dp)
                    ) {
                        Text("Logout")
                    }
                }

                content(padding)
            }
        }
    }
}



