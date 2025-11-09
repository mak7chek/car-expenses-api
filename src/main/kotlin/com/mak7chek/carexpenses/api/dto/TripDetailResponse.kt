package com.mak7chek.carexpenses.api.dto

import com.mak7chek.carexpenses.api.dto.RoutePointResponse
import com.mak7chek.carexpenses.api.model.FuelType
import java.time.LocalDateTime

data class TripDetailResponse(
    val id: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val notes: String?,
    val vehicleName: String,
    val fuelType: FuelType,

    val totalDistanceKm: Double,
    val avgConsumption: Double,
    val totalFuelConsumedL: Double,
    val pricePerLiter: Double,
    val totalCost: Double,
    val routePoints: List<RoutePointResponse>
)