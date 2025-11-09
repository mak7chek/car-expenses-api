package com.mak7chek.carexpenses.api.repository

import com.mak7chek.carexpenses.api.model.Trip
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TripRepository : JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {

    fun findByUserId(userId: Long): List<Trip>
    fun deleteAllByUserId(userId: Long)

    @Query("SELECT t FROM Trip t JOIN FETCH t.user u JOIN FETCH t.vehicle v LEFT JOIN FETCH t.routePoints WHERE t.id = :id")
    fun findTripWithDetails(@Param("id") id: Long): Optional<Trip>
}