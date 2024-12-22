package com.example.fryzjer.data.model

sealed class ReservationState {
    object Loading : ReservationState()
    data class Success(val message: String) : ReservationState()
    data class Error(val message: String) : ReservationState()
}