package com.mak7chek.carexpenses.api.util

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object HaversineCalculator {

    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Розраховує відстань між двома GPS-точками в кілометрах.
     */
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1Rad) * cos(lat2Rad)
        val c = 2 * asin(sqrt(a))

        return EARTH_RADIUS_KM * c
    }
}