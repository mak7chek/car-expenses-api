package com.mak7chek.carexpenses.api.dto

import java.time.LocalDateTime

data class TripResponse(
    val id: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val totalDistanceKm: Double,
    val totalFuelConsumedL: Double,
    val notes: String?,
    val vehicleName: String,
    val vehicleId: Long
)