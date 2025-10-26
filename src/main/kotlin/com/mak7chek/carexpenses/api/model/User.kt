package com.mak7chek.carexpenses.api.model
import jakarta.persistence.*
@Entity
@Table(name = "users")
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String? = null,
    @Column(unique = true, nullable = false)
    val email: String,
    @Column(nullable = false)
    val passwordHash: String
)