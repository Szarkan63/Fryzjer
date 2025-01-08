package com.example.fryzjer.data.model
// File: UserViewModel.kt

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val _userId = mutableStateOf<String?>(null)
    val userId: State<String?> get() = _userId

    fun setUserId(id: String?) {
        _userId.value = id
    }
}

