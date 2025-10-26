package com.mak7chek.carexpenses.api.service

import com.mak7chek.carexpenses.api.dto.VehicleRequest
import com.mak7chek.carexpenses.api.dto.VehicleResponse
import com.mak7chek.carexpenses.api.model.Vehicle
import com.mak7chek.carexpenses.api.repository.UserRepository
import com.mak7chek.carexpenses.api.repository.VehicleRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) {
    // ---- Приватна helper-функція для мапінгу ----
    private fun Vehicle.toResponse(): VehicleResponse {
        return VehicleResponse(
            id = this.id!!,
            name = this.name,
            make = this.make,
            model = this.model,
            year = this.year,
            avgConsumptionLitersPer100Km = this.avgConsumptionLitersPer100Km
        )
    }
    // ---- Публічні методи ----

    @Transactional(readOnly = true)
    fun getVehiclesForUser(userEmail: String): List<VehicleResponse> {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { UsernameNotFoundException("Користувача не знайдено") }

        return vehicleRepository.findByUserId(user.id!!)
            .map { it.toResponse() }
    }

    @Transactional
    fun createVehicle(request: VehicleRequest, userEmail: String): VehicleResponse {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { UsernameNotFoundException("Користувача не знайдено") }

        val newVehicle = Vehicle(
            name = request.name,
            make = request.make,
            model = request.model,
            year = request.year,
            avgConsumptionLitersPer100Km = request.avgConsumptionLitersPer100Km,
            user = user
        )

        val savedVehicle = vehicleRepository.save(newVehicle)
        return savedVehicle.toResponse()
    }

    @Transactional
    fun updateVehicle(vehicleId: Long, request: VehicleRequest, userEmail: String): VehicleResponse {
        val vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow { NoSuchElementException("Автомобіль з ID $vehicleId не знайдено") }

        if (vehicle.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цього автомобіля")
        }

        val updatedVehicle = vehicle.copy(
            name = request.name,
            make = request.make,
            model = request.model,
            year = request.year,
            avgConsumptionLitersPer100Km = request.avgConsumptionLitersPer100Km
        )

        val savedVehicle = vehicleRepository.save(updatedVehicle)
        return savedVehicle.toResponse()
    }

    @Transactional
    fun deleteVehicle(vehicleId: Long, userEmail: String) {
        val vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow { NoSuchElementException("Автомобіль з ID $vehicleId не знайдено") }

        if (vehicle.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цього автомобіля")
        }

        vehicleRepository.delete(vehicle)
    }
}