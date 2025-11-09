package com.mak7chek.carexpenses.api.dto

// 1. Не забудь імпортувати свій Enum
import com.mak7chek.carexpenses.api.model.FuelType

data class VehicleRequest(
    val name: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val avgConsumptionLitersPer100Km: Double,
    val fuelType: FuelType?
)