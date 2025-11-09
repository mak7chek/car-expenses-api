package com.mak7chek.carexpenses.api.dto

import com.mak7chek.carexpenses.api.model.FuelType

data class FuelPriceUpdateRequest(
    val fuelType: FuelType,
    val price: Double
)

data class FuelPriceResponse(
    val fuelType: FuelType,
    val price: Double
)