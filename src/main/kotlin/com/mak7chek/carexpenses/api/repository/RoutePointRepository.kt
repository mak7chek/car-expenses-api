package com.mak7chek.carexpenses.api.repository

import com.mak7chek.carexpenses.api.model.RoutePoint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoutePointRepository : JpaRepository<RoutePoint, Long> {

    fun findAllByTripId(tripId: Long): List<RoutePoint>
}