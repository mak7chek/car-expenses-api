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
import org.springframework.security.core.userdetails.UserDetails

@RestController
@RequestMapping("/api/vehicles")
class VehicleController(
    private val vehicleService: VehicleService
) {

    @PostMapping
    fun createVehicle(
        @RequestBody request: VehicleRequest,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<VehicleResponse> {
        val userEmail = userDetails?.username
            ?: throw AccessDeniedException("Доступ заборонено: не вдалося ідентифікувати користувача")

        val response = vehicleService.createVehicle(request, userEmail)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }


    @GetMapping
    fun getUserVehicles(@AuthenticationPrincipal userDetails: UserDetails?): ResponseEntity<List<VehicleResponse>> {

        val userEmail = userDetails?.username
            ?: throw AccessDeniedException("Доступ заборонено: не вдалося ідентифікувати користувача")

        val vehicles = vehicleService.getVehiclesForUser(userEmail)
        return ResponseEntity.ok(vehicles)
    }


    @PutMapping("/{id}")
    fun updateVehicle(
        @PathVariable id: Long,
        @RequestBody request: VehicleRequest,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<Any> {
        return try {
            // 2. Безпечно дістаємо email
            val userEmail = userDetails?.username
                ?: throw AccessDeniedException("Доступ заборонено: не вдалося ідентифікувати користувача")

            val updatedVehicle = vehicleService.updateVehicle(id, request, userEmail)
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
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<Any> {
        return try {
            // 2. Безпечно дістаємо email
            val userEmail = userDetails?.username
                ?: throw AccessDeniedException("Доступ заборонено: не вдалося ідентифікувати користувача")

            vehicleService.deleteVehicle(id, userEmail)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }
}