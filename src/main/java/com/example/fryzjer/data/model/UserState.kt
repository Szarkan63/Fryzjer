package com.example.fryzjer.data.model

sealed class UserState {
    object Loading: UserState()
    data class Success(val message: String,val isRegistration: Boolean = false): UserState()
    data class Error(val message: String): UserState()
}