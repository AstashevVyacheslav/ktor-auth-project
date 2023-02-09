package ru.astashev.data.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String
)