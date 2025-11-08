package com.mak7chek.carexpenses.api.dto


data class UpdatePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)