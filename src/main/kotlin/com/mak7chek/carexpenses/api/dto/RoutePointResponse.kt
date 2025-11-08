package com.mak7chek.carexpenses.api.dto

import java.time.LocalDateTime

data class RoutePointResponse (
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
)