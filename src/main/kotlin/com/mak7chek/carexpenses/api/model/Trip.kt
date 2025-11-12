package com.mak7chek.carexpenses.api.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "trips")
data class Trip(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
    var totalDistanceKm: Double = 0.0,
    var totalFuelConsumedL: Double = 0.0,
    var notes: String? = null,

    @Column(nullable = true)
    var fuelPriceAtCreation: Double? = null,
    @Column(nullable = true)
    var calculatedTotalCost: Double? = null,
    // --- Зв'язки ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    val vehicle: Vehicle,
    @OneToMany(mappedBy = "trip", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val routePoints: List<RoutePoint> = emptyList()

)