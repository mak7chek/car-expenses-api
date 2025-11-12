package com.mak7chek.carexpenses.api.service

import com.mak7chek.carexpenses.api.controller.NoteUpdateRequest
import com.mak7chek.carexpenses.api.dto.*
import com.mak7chek.carexpenses.api.model.FuelType
import com.mak7chek.carexpenses.api.model.RoutePoint
import com.mak7chek.carexpenses.api.model.Trip
import com.mak7chek.carexpenses.api.repository.FuelPriceRepository
import com.mak7chek.carexpenses.api.repository.RoutePointRepository
import com.mak7chek.carexpenses.api.repository.TripRepository
import com.mak7chek.carexpenses.api.repository.UserRepository
import com.mak7chek.carexpenses.api.repository.VehicleRepository
import com.mak7chek.carexpenses.api.util.HaversineCalculator
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class TripService(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val fuelPriceRepository: FuelPriceRepository,
    private val routePointRepository: RoutePointRepository
) {

    // --- DTO Маппери ---

    private fun Trip.toResponse(): TripResponse {
        return TripResponse(
            id = this.id!!,
            startTime = this.startTime,
            endTime = this.endTime,
            totalDistanceKm = this.totalDistanceKm,
            totalFuelConsumedL = this.totalFuelConsumedL,
            notes = this.notes,
            vehicleName = this.vehicle.name,
            vehicleId = this.vehicle.id!!
        )
    }

    private fun Trip.toDetailResponse(pricePerLiter: Double, totalCost: Double): TripDetailResponse {
        val vehicle = this.vehicle
        val vehicleFuelType = vehicle.fuelType
            ?: throw IllegalStateException("Помилка: Автомобіль ${vehicle.name} (ID: ${vehicle.id}) не має типу палива. Завершіть міграцію.")
        return TripDetailResponse(
            id = this.id!!,
            startTime = this.startTime,
            endTime = this.endTime,
            notes = this.notes,
            vehicleName = this.vehicle.name,
            fuelType = vehicleFuelType,
            totalDistanceKm = this.totalDistanceKm,
            avgConsumption = this.vehicle.avgConsumptionLitersPer100Km,
            totalFuelConsumedL = this.totalFuelConsumedL,
            pricePerLiter = pricePerLiter,
            totalCost = totalCost,
            routePoints = this.routePoints.sortedBy { it.timestamp }.map { it.toResponse() }
        )
    }

    private fun RoutePoint.toResponse(): RoutePointResponse {
        return RoutePointResponse(
            latitude = this.latitude,
            longitude = this.longitude,
            timestamp = this.timestamp
        )
    }


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

        routePointRepository.saveAll(newPoints)
    }

    @Transactional
    fun endTrip(tripId: Long, userEmail: String): ResponseEntity<Any> {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Поїздку з ID $tripId не знайдено") }

        if (trip.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цієї поїздки")
        }

        // ... ваша логіка розрахунку дистанції 'totalDistance' ...
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

        // --- ⬇️ НОВА ЛОГІКА "ЗАМОРОЗКИ" ⬇️ ---

        // 1. Отримуємо тип палива
        val vehicle = trip.vehicle
        val vehicleFuelType = vehicle.fuelType
            ?: throw IllegalStateException("Помилка: Автомобіль ${vehicle.name} (ID: ${vehicle.id}) не має типу палива.")

        // 2. Отримуємо ПОТОЧНУ ціну
        val fuelPrice = fuelPriceRepository.findByUserAndFuelType(trip.user, vehicleFuelType)
            .orElse(null)
        val pricePerLiter = fuelPrice?.pricePerLiter ?: 0.0 // Використовуємо 0.0, якщо не задано

        // 3. Розраховуємо фінальну вартість
        val totalCost = totalFuel * pricePerLiter

        // 4. Зберігаємо всі розраховані значення
        val finishedTrip = trip.copy(
            endTime = LocalDateTime.now(),
            totalDistanceKm = totalDistance,
            totalFuelConsumedL = totalFuel,
            fuelPriceAtCreation = pricePerLiter, // ⬅️ Зберегли
            calculatedTotalCost = totalCost      // ⬅️ Зберегли
        )

        tripRepository.save(finishedTrip)
        return ResponseEntity.ok(mapOf("message" to "Поїздку завершено"))
    }

    @Transactional(readOnly = true)
    fun getTripsForUser(
        userEmail: String,
        search: String?,
        vehicleId: Long?,
        dateFrom: LocalDate?,
        dateTo: LocalDate?,
        minDistance: Double?,
        maxDistance: Double?
    ): List<TripResponse> {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { UsernameNotFoundException("Користувача не знайдено") }

        var spec = TripSpecification.hasUser(user)

        spec = spec.and(TripSpecification.containsNote(search))
        spec = spec.and(TripSpecification.hasVehicle(vehicleId))
        spec = spec.and(TripSpecification.isBetweenDates(dateFrom, dateTo))
        spec = spec.and(TripSpecification.isDistanceBetween(minDistance, maxDistance))

        return tripRepository.findAll(spec)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getTripDetails(tripId: Long, userEmail: String): TripDetailResponse {
        val trip = tripRepository.findTripWithDetails(tripId)
            .orElseThrow { NoSuchElementException("Поїздку з ID $tripId не знайдено") }

        if (trip.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цієї поїздки")
        }

        val pricePerLiter = trip.fuelPriceAtCreation ?: 0.0
        val totalCost = trip.calculatedTotalCost ?: 0.0

        return trip.toDetailResponse(pricePerLiter, totalCost)
    }

    @Transactional
    fun updateTripNotes(tripId: Long, userEmail: String, request: NoteUpdateRequest) {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Поїздку з ID $tripId не знайдено") }

        if (trip.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цієї поїздки")
        }

        trip.notes = request.notes
        tripRepository.save(trip)
    }


    @Transactional
    fun deleteTrip(tripId: Long, userEmail: String) {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Поїздку з ID $tripId не знайдено") }

        if (trip.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цієї поїздки")
        }

        tripRepository.delete(trip)
    }
    @Transactional
    fun updateTripPrice(tripId: Long, userEmail: String, newPricePerLiter: Double): TripDetailResponse {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Поїздку з ID $tripId не знайдено") }

        if (trip.user.email != userEmail) {
            throw AccessDeniedException("Ви не маєте доступу до цієї поїздки")
        }

        if (trip.endTime == null) {
            throw IllegalStateException("Неможливо оновити ціну для поїздки, яка ще не завершена.")
        }

        // Перераховуємо загальну вартість з новою ціною
        val newTotalCost = trip.totalFuelConsumedL * newPricePerLiter

        // Оновлюємо "заморожені" поля
        trip.fuelPriceAtCreation = newPricePerLiter
        trip.calculatedTotalCost = newTotalCost

        val savedTrip = tripRepository.save(trip)

        // Повертаємо оновлені деталі
        return savedTrip.toDetailResponse(newPricePerLiter, newTotalCost)
    }

    @Transactional
    fun backfillTripPrices(userEmail: String): Map<String, Any> {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { UsernameNotFoundException("Користувача не знайдено") }

        // 1. Знаходимо всі "старі" завершені поїздки, де немає ціни
        val tripsToUpdate = tripRepository.findByUserAndCalculatedTotalCostIsNullAndEndTimeIsNotNull(user)

        if (tripsToUpdate.isEmpty()) {
            return mapOf("message" to "Не знайдено старих поїздок для оновлення. Все вже актуально.", "updatedCount" to 0)
        }

        var updatedCount = 0
        val tripsToSave = mutableListOf<Trip>()

        // 2. Створимо кеш цін, щоб не питати базу 100 разів про одне й те саме
        val priceCache = mutableMapOf<FuelType, Double>()

        for (trip in tripsToUpdate) {
            val vehicle = trip.vehicle
            // Якщо у авто не вказано тип палива, ми не можемо розрахувати ціну.
            val fuelType = vehicle.fuelType ?: continue

            // 3. Беремо ціну з кешу або (якщо немає) з бази
            //    Використовуємо ПОТОЧНУ ціну як найкраще припущення для старих поїздок
            val pricePerLiter = priceCache.getOrPut(fuelType) {
                fuelPriceRepository.findByUserAndFuelType(user, fuelType)
                    .map { it.pricePerLiter }
                    .orElse(0.0) // Якщо ціни немає, ставимо 0.0
            }

            // 4. Розраховуємо вартість
            val totalCost = trip.totalFuelConsumedL * pricePerLiter

            // 5. Оновлюємо "заморожені" поля
            trip.apply {
                fuelPriceAtCreation = pricePerLiter
                calculatedTotalCost = totalCost
            }
            tripsToSave.add(trip)
            updatedCount++
        }

        // 6. Зберігаємо всі оновлені поїздки одним запитом
        tripRepository.saveAll(tripsToSave)

        return mapOf(
            "message" to "Успішно оновлено $updatedCount старих поїздок.",
            "updatedCount" to updatedCount
        )
    }
}