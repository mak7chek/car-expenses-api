package com.mak7chek.carexpenses.api.model

import jakarta.persistence.*

@Entity
@Table(name = "fuel_prices", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_id", "fuelType"])
])
data class FuelPrice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val fuelType: FuelType,

    @Column(nullable = false)
    val pricePerLiter: Double
)