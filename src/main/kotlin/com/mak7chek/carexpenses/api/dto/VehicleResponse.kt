package com.mak7chek.carexpenses.api.dto

data class VehicleResponse(
    val id: Long,
    val name: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val avgConsumptionLitersPer100Km: Double
)