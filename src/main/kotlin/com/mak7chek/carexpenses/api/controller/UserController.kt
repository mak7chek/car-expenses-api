// /api/controller/UserController.kt
package com.mak7chek.carexpenses.api.controller

import com.mak7chek.carexpenses.api.dto.UpdateNameRequest
import com.mak7chek.carexpenses.api.dto.UpdatePasswordRequest
import com.mak7chek.carexpenses.api.model.User
import com.mak7chek.carexpenses.api.repository.UserRepository
import com.mak7chek.carexpenses.api.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val userRepository: UserRepository
) {

    // --- Ендпоінт для зміни імені ---
    @PutMapping("/name")
    fun updateName(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: UpdateNameRequest
    ): ResponseEntity<Map<String, String>> {

        val user = findUserFromDetails(userDetails)
        val updatedUser = userService.updateUserName(user.id!!, request)

        return ResponseEntity.ok(mapOf(
            "message" to "Ім'я успішно оновлено",
            // 3. ВИПРАВЛЯЄМО ПОМИЛКУ NULLABLE
            "newName" to (updatedUser.name ?: "")
        ))
    }

    // --- Ендпоінт для зміни пароля ---
    @PutMapping("/password")
    fun updatePassword(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: UpdatePasswordRequest
    ): ResponseEntity<Map<String, String>> {
        val user = findUserFromDetails(userDetails)
        userService.updateUserPassword(user.id!!, request)
        return ResponseEntity.ok(mapOf("message" to "Пароль успішно оновлено"))
    }

    @DeleteMapping("/me")
    fun deleteAccount(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<String, String>> {
        val user = findUserFromDetails(userDetails)
        userService.deleteUserAccount(user.id!!)
        return ResponseEntity.ok(mapOf("message" to "Акаунт успішно видалено"))
    }

    private fun findUserFromDetails(userDetails: UserDetails): User {
        val email = userDetails.username
        return userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("Авторизований користувач '$email' не знайдений в базі") }
    }
}