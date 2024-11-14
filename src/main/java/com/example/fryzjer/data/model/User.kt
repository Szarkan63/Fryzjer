package com.example.fryzjer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    val name: String,
    val surname: String,
    val email: String
)
