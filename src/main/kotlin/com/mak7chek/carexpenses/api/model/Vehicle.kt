package com.mak7chek.carexpenses.api.model

import jakarta.persistence.*

@Entity
@Table(name = "vehicles")
data class Vehicle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    val make: String? = null, // "Audi"
    val model: String? = null, // "A6"
    val year: Int? = null,
    @Column(nullable = false)
    val avgConsumptionLitersPer100Km: Double,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    val fuelType: FuelType?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User
)