package com.mak7chek.carexpenses.api.controller

import com.mak7chek.carexpenses.api.dto.FuelPriceResponse
import com.mak7chek.carexpenses.api.dto.FuelPriceUpdateRequest
import com.mak7chek.carexpenses.api.model.FuelPrice
import com.mak7chek.carexpenses.api.model.User
import com.mak7chek.carexpenses.api.repository.FuelPriceRepository
import com.mak7chek.carexpenses.api.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/fuel-prices")
class FuelPriceController(
    private val fuelPriceRepository: FuelPriceRepository,
    private val userRepository: UserRepository
) {

    @GetMapping
    fun getMyPrices(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<FuelPriceResponse>> {
        val user = getUser(userDetails)
        val prices = fuelPriceRepository.findAllByUser(user)

        val response = prices.map { FuelPriceResponse(it.fuelType, it.pricePerLiter) }
        return ResponseEntity.ok(response)
    }


    @PostMapping
    @Transactional
    fun updatePrices(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody requests: List<FuelPriceUpdateRequest>
    ): ResponseEntity<Any> {
        val user = getUser(userDetails)

        for (req in requests) {
            val existingPrice = fuelPriceRepository.findByUserAndFuelType(user, req.fuelType)

            if (existingPrice.isPresent) {
                val updated = existingPrice.get().copy(pricePerLiter = req.price)
                fuelPriceRepository.save(updated)
            } else {
                val newPrice = FuelPrice(user = user, fuelType = req.fuelType, pricePerLiter = req.price)
                fuelPriceRepository.save(newPrice)
            }
        }
        return ResponseEntity.ok().build()
    }

    private fun getUser(userDetails: UserDetails): User {
        return userRepository.findByEmail(userDetails.username)
            .orElseThrow { IllegalArgumentException("User not found") }
    }
}