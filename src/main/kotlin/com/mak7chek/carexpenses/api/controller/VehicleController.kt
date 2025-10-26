package com.mak7chek.carexpenses.api.controller

import com.mak7chek.carexpenses.api.dto.VehicleRequest
import com.mak7chek.carexpenses.api.dto.VehicleResponse
import com.mak7chek.carexpenses.api.service.VehicleService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/vehicles")
class VehicleController(
    private val vehicleService: VehicleService
) {


    @PostMapping
    fun createVehicle(
        @RequestBody request: VehicleRequest,
        @AuthenticationPrincipal principal: Principal
    ): ResponseEntity<VehicleResponse> {
        val response = vehicleService.createVehicle(request, principal.name)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getUserVehicles(@AuthenticationPrincipal principal: Principal): ResponseEntity<List<VehicleResponse>> {
        val vehicles = vehicleService.getVehiclesForUser(principal.name)
        return ResponseEntity.ok(vehicles)
    }

    @PutMapping("/{id}") // Наприклад, /api/vehicles/5
    fun updateVehicle(
        @PathVariable id: Long,
        @RequestBody request: VehicleRequest,
        @AuthenticationPrincipal principal: Principal
    ): ResponseEntity<Any> { // <-- ВИПРАВЛЕНО ТУТ
        return try {
            val updatedVehicle = vehicleService.updateVehicle(id, request, principal.name)
            ResponseEntity.ok(updatedVehicle)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteVehicle(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: Principal
    ): ResponseEntity<Any> {
        return try {
            vehicleService.deleteVehicle(id, principal.name)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }
}