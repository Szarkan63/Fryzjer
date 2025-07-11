package com.example.fryzjer.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fryzjer.SupabaseAuthViewModel
import com.example.fryzjer.data.network.SupabaseClient
import kotlinx.coroutines.launch

val adminId = "0b94d3b8-2509-4a95-934f-2434f075791b" // Replace with your actual admin user ID

@Composable
fun HomeScreen(
    viewModel: SupabaseAuthViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    var username = remember { mutableStateOf<String?>(null) }
    var userId = remember { mutableStateOf<String?>(null) } // Store user ID
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
            Log.d("Drawer", "Drawer closed")
        }
    }

    LaunchedEffect(Unit) {
        val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
        val firstName = user.userMetadata?.get("first_name") ?: "Unknown"
        userId.value = user.id // Store the user ID for comparison
        Log.d("HomeScreen", "User ID: ${user.id}, First Name: $firstName")
        username.value = firstName.toString().trim('"')
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    navController = navController,
                    userId = userId.value // Pass userId here
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Strona główna",
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
                        text = "Welcome to the Home Screen",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Hello, ${username.value}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 20.sp
                    )
                }

                Button(
                    onClick = {
                        viewModel.logout(context)
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
        }
    }
}

@Composable
fun DrawerContent(
    navController: NavController,
    userId: String?,  // Receive userId from ViewModel
    modifier: Modifier = Modifier
) {
    val isAdmin = userId == adminId // Check if the user is an admin

    // Log the current userId and adminId
    Log.d("DrawerContent", "User ID: $userId, Admin ID: $adminId, Is Admin: $isAdmin")

    Text(
        text = "Menu",
        fontSize = 24.sp,
        modifier = modifier.padding(16.dp)
    )
    HorizontalDivider()
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = Icons.Rounded.Home,
                contentDescription = "Strona główna",
            )
        },
        label = {
            Text(
                text = "Strona główna",
                fontSize = 17.sp,
                modifier = modifier.padding(16.dp)
            )
        },
        selected = false,
        onClick = {
            navController.navigate("home")
        }
    )
    Spacer(modifier = Modifier.height(8.dp))

    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = "Rezerwacje",
            )
        },
        label = {
            Text(
                text = "Twoje rezerwacje",
                fontSize = 17.sp,
                modifier = modifier.padding(16.dp)
            )
        },
        selected = false,
        onClick = {
            navController.navigate("Reservations")
        }
    )

    Spacer(modifier = Modifier.height(8.dp))

    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = "Zloz rezerwacje",
            )
        },
        label = {
            Text(
                text = "Zloz rezerwacje",
                fontSize = 17.sp,
                modifier = modifier.padding(16.dp)
            )
        },
        selected = false,
        onClick = {
            navController.navigate("makeReservation")
        }
    )

    // Show Admin Panel only if the user is an admin
    if (isAdmin) {
        Spacer(modifier = Modifier.height(8.dp))
        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = "Admin Panel",
                )
            },
            label = {
                Text(
                    text = "Admin Panel",
                    fontSize = 17.sp,
                    modifier = modifier.padding(16.dp)
                )
            },
            selected = false,
            onClick = {
                navController.navigate("adminPanel")
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onOpenDrawer: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.6f)
        ),
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .size(28.dp)
                    .clickable { onOpenDrawer() }
            )
        },
        title = {
            Text(text = title)
        },
        actions = {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Menu",
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(28.dp)
            )
        }
    )
}








