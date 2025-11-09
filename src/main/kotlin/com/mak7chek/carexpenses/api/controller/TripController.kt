package com.mak7chek.carexpenses.api.controller

import com.mak7chek.carexpenses.api.dto.*
import com.mak7chek.carexpenses.api.service.TripService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

data class NoteUpdateRequest(val notes: String?)


@RestController
@RequestMapping("/api/v1/trips")
class TripController(
    private val tripService: TripService
) {

    private fun getUserEmail(userDetails: UserDetails?): String {
        return userDetails?.username
            ?: throw AccessDeniedException("Доступ заборонено: не вдалося ідентифікувати користувача")
    }

    @PostMapping("/start")
    fun startTrip(
        @RequestBody request: TripStartRequest,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<Any> {
        return try {
            val userEmail = getUserEmail(userDetails)
            val tripResponse = tripService.startTrip(userEmail, request)
            ResponseEntity.status(HttpStatus.CREATED).body(tripResponse)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @PostMapping("/{id}/track")
    fun addTrackPoints(
        @PathVariable id: Long,
        @RequestBody request: TrackBatchRequest,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<Any> {
        return try {
            val userEmail = getUserEmail(userDetails)
            tripService.addTrackPoints(id, userEmail, request)
            ResponseEntity.ok().body(mapOf("message" to "Точки успішно додано"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
        }
    }

    @PostMapping("/{id}/end")
    fun endTrip(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<Any> {
        return try {
            val userEmail = getUserEmail(userDetails)
            tripService.endTrip(id, userEmail)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }

    @GetMapping
    fun getAllUserTrips(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) vehicleId: Long?,
        @RequestParam(required = false) dateFrom: LocalDate?,
        @RequestParam(required = false) dateTo: LocalDate?
    ): ResponseEntity<List<TripResponse>> {
        val userEmail = getUserEmail(userDetails)
        val trips = tripService.getTripsForUser(userEmail, search, vehicleId, dateFrom, dateTo)
        return ResponseEntity.ok(trips)
    }

    @GetMapping("/{id}")
    fun getTripDetails(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<TripDetailResponse> {

        val userEmail = getUserEmail(userDetails)
        val trip = tripService.getTripDetails(id, userEmail)
        return ResponseEntity.ok(trip)
    }

    @PutMapping("/{id}/notes")
    fun updateTripNotes(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody request: NoteUpdateRequest
    ): ResponseEntity<Any> {
        return try {
            val userEmail = getUserEmail(userDetails)
            tripService.updateTripNotes(id, userEmail, request)
            ResponseEntity.ok(mapOf("message" to "Нотатку оновлено"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteTrip(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<Any> {
        return try {
            val userEmail = getUserEmail(userDetails)
            tripService.deleteTrip(id, userEmail)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }
}