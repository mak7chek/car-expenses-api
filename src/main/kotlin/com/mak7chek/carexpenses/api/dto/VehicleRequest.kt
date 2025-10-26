package com.mak7chek.carexpenses.api.dto

data class VehicleRequest(
    val name: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val avgConsumptionLitersPer100Km: Double
)