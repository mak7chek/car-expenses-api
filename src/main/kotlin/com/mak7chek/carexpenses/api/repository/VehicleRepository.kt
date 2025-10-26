package com.mak7chek.carexpenses.api.repository

import com.mak7chek.carexpenses.api.model.Vehicle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VehicleRepository : JpaRepository<Vehicle, Long> {

    fun findByUserId(userId: Long): List<Vehicle>
}