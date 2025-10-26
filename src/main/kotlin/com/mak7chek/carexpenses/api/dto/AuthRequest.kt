package com.mak7chek.carexpenses.api.dto

data class AuthRequest(
    val email: String,
    val password: String,
    val name: String? = null
)