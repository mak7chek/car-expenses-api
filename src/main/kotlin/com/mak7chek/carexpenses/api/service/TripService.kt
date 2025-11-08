package com.mak7chek.carexpenses.api.service

import com.mak7chek.carexpenses.api.dto.*
import com.mak7chek.carexpenses.api.model.RoutePoint
import com.mak7chek.carexpenses.api.model.Trip
import com.mak7chek.carexpenses.api.repository.TripRepository
import com.mak7chek.carexpenses.api.repository.UserRepository
import com.mak7chek.carexpenses.api.repository.VehicleRepository
import com.mak7chek.carexpenses.api.util.HaversineCalculator
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TripService(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) {

    // --- DTO Маппери (хелпер-функції) ---

    private fun RoutePoint.toResponse(): RoutePointResponse {
        return RoutePointResponse(
            latitude = this.latitude,
            longitude = this.longitude,
            timestamp = this.timestamp
        )
    }

    private fun Trip.toResponse(): TripResponse {
        return TripResponse(
            id = this.id!!,
            startTime = this.startTime,
            endTime = this.endTime,
            totalDistanceKm = this.totalDistanceKm,
            totalFuelConsumedL = this.totalFuelConsumedL,
            notes = this.notes,
            vehicleName = this.vehicle.name,
            vehicleId = this.vehicle.id!!,
            routePoints = this.routePoints.map { it.toResponse() }
        )
    }

    // --- CRUD Методи ---

    @Transactional
    fun startTrip(userEmail: String, request: TripStartRequest): TripResponse {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { UsernameNotFoundException("Користувача не знайдено") }

        val vehicle = vehicleRepository.findById(request.vehicleId)
            .orElseThrow { NoSuchElementException("Автомобіль з ID ${request.vehicleId} не знайдено") }

        if (vehicle.user.id != user.id) {
            throw AccessDeniedException("Це авто належить іншому користувачу")
        }

        val newTrip = Trip(
            startTime = LocalDateTime.now(),
            user = user,
            vehicle = vehicle
        )

        val savedTrip = tripRepository.saveAndFlush(newTrip)
        val reloadedTrip = tripRepository.findById(savedTrip.id!!)
            .orElseThrow { IllegalStateException("Щойно збережена поїздка зникла") }

        return reloadedTrip.toResponse()
    }

    @Transactional
    fun addTrackPoints(tripId: Long, userEmail: String, request: TrackBatchRequest) {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Поїздку з ID $tripId не знайдено") }

        // Перевірка безпеки
        if (trip.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цієї поїздки")
        }

        if (trip.endTime != null) {
            throw IllegalStateException("Неможливо додати точки: поїздка з ID $tripId вже завершена.")
        }

        val newPoints = request.points.map {
            RoutePoint(
                latitude = it.latitude,
                longitude = it.longitude,
                timestamp = it.timestamp,
                trip = trip
            )
        }

        val updatedTrip = trip.copy(
            routePoints = trip.routePoints + newPoints
        )

        tripRepository.save(updatedTrip)
    }

    @Transactional
    fun endTrip(tripId: Long, userEmail: String): TripResponse {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Поїздку з ID $tripId не знайдено") }

        // Перевірка безпеки
        if (trip.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цієї поїздки")
        }

        // --- ГОЛОВНА ЛОГІКА РОЗРАХУНКІВ ---

        val points = trip.routePoints.sortedBy { it.timestamp }
        var totalDistance = 0.0

        if (points.size > 1) {
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i+1]
                totalDistance += HaversineCalculator.calculateDistance(
                    p1.latitude, p1.longitude,
                    p2.latitude, p2.longitude
                )
            }
        }

        val consumptionPer100Km = trip.vehicle.avgConsumptionLitersPer100Km
        val totalFuel = (totalDistance / 100.0) * consumptionPer100Km

        val finishedTrip = trip.copy(
            endTime = LocalDateTime.now(),
            totalDistanceKm = totalDistance,
            totalFuelConsumedL = totalFuel
        )

        val savedTrip = tripRepository.save(finishedTrip)
        return savedTrip.toResponse()
    }

    @Transactional(readOnly = true)
    fun getTripsForUser(userEmail: String): List<TripResponse> {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { UsernameNotFoundException("Користувача не знайдено") }

        return tripRepository.findByUserId(user.id!!)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getTripDetails(tripId: Long, userEmail: String): TripResponse {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Поїздку з ID $tripId не знайдено") }

        // Перевірка безпеки
        if (trip.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цієї поїздки")
        }

        return trip.toResponse()
    }

    @Transactional
    fun deleteTrip(tripId: Long, userEmail: String) {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Поїздку з ID $tripId не знайдено") }

        // Перевірка безпеки
        if (trip.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цієї поїздки")
        }

        tripRepository.delete(trip)
    }
}