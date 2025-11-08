package com.mak7chek.carexpenses.api.repository

import com.mak7chek.carexpenses.api.model.Trip
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : JpaRepository<Trip, Long> {

    fun findByUserId(userId: Long): List<Trip>
    fun deleteAllByUserId(userId: Long)

}