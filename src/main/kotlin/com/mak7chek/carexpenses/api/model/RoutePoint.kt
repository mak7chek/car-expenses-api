package com.mak7chek.carexpenses.api.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "route_points")
data class RoutePoint(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val latitude: Double,
    val longitude: Double,
    val timestamp: LocalDateTime,

    // --- Зв'язок ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    val trip: Trip
)