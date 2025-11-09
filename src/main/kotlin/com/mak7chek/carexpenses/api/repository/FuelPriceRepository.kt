package com.mak7chek.carexpenses.api.repository

import com.mak7chek.carexpenses.api.model.FuelPrice
import com.mak7chek.carexpenses.api.model.FuelType
import com.mak7chek.carexpenses.api.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FuelPriceRepository : JpaRepository<FuelPrice, Long> {
    fun findByUserAndFuelType(user: User, fuelType: FuelType): Optional<FuelPrice>

    fun findAllByUser(user: User): List<FuelPrice>
}