package com.example.fryzjer

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fryzjer.data.model.UserState
import com.example.fryzjer.data.network.SupabaseClient
import com.example.fryzjer.utils.SharedPreferenceHelper
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.providers.builtin.Email


class SupabaseAuthViewModel : ViewModel() {
    private val _userState = mutableStateOf<UserState>(UserState.Loading)
    val userState: State<UserState> = _userState

    private val client=SupabaseClient.auth

    // Funkcja do walidacji emaila
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }

    fun signUp(
        context: Context,
        userEmail: String,
        userPassword: String
    ) {
        viewModelScope.launch {
            _userState.value = UserState.Loading

            // Sprawdzamy, czy email jest w poprawnym formacie
            if (!isValidEmail(userEmail)) {
                _userState.value = UserState.Error("Invalid email format.")
                return@launch
            }

            try {
                // Using the correct signUp method
                val result = client.signUpWith(Email) {
                    email = userEmail
                    password = userPassword
                }

                // Check for errors in the result
                if (result != null) {
                    _userState.value = UserState.Error("Error occurred during registration.")
                } else {
                    saveToken(context)
                    _userState.value = UserState.Success("Registered user successfully!",isRegistration = true)
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

    // Save the current token to SharedPreferences
    private fun saveToken(context: Context) {
        viewModelScope.launch {
            try {
                // Check if the auth client has a different method for obtaining the current session
                val session = client.currentSessionOrNull() // Replace with actual method

                if (session?.accessToken != null) {
                    val accessToken = session.accessToken
                    val sharedPref = SharedPreferenceHelper(context)
                    sharedPref.saveStringData("accessToken", accessToken)
                } else {
                    _userState.value = UserState.Error("Error: Access token not found")
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }


    fun login(
        context: Context,
        userEmail: String,
        userPassword: String
    ) {
        viewModelScope.launch {
            _userState.value=UserState.Loading
            try {
                client.signInWith(Email){
                    email=userEmail
                    password=userPassword
                }
                saveToken(context)
                _userState.value=UserState.Success("Logged in succesfully!",isRegistration = false)
        } catch (e: Exception){
            _userState.value=UserState.Error("Error: ${e.message}")
            }
        }
    }


    // Logout method
    fun logout(context: Context) {
        val sharedPref = SharedPreferenceHelper(context)
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                // Check if user is logged in
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _userState.value = UserState.Error("Nie mozesz sie wylogowac,nie jest zalogowany!")
                    return@launch
                }

                // Proceed with logout if the user is logged in
                client.signOut()
                sharedPref.clearPreferences()
                _userState.value = UserState.Success("Nie jest zalogowany!")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

    // Check if the user is logged in
    fun isUserLoggedIn(
        context: Context
    ) {
        viewModelScope.launch {
            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _userState.value = UserState.Error("User is not logged in!")
                }
                    else {
                        client.retrieveUser(token)
                        client.refreshCurrentSession()
                         saveToken(context)
                        _userState.value = UserState.Success("User is already logged in!")
                    }
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

    // Retrieve the token from SharedPreferences
    private fun getToken(context: Context): String? {
        val sharedPref = SharedPreferenceHelper(context)
        return sharedPref.getStringData("accessToken")
    }
}







