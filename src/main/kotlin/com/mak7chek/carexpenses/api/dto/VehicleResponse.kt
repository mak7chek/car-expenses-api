package com.mak7chek.carexpenses.api.dto

import com.mak7chek.carexpenses.api.model.FuelType

data class VehicleResponse(
    val id: Long,
    val name: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val avgConsumptionLitersPer100Km: Double,
    val fuelType: FuelType?
)