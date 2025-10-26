package com.mak7chek.carexpenses.api.dto

data class AuthResponse(
    val userId: Long,
    val email: String,
    val message: String
)