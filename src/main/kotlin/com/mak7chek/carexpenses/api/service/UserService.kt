package com.mak7chek.carexpenses.api.service

import com.mak7chek.carexpenses.api.dto.UpdateNameRequest
import com.mak7chek.carexpenses.api.dto.UpdatePasswordRequest
import com.mak7chek.carexpenses.api.model.User
import com.mak7chek.carexpenses.api.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.mak7chek.carexpenses.api.repository.VehicleRepository
import com.mak7chek.carexpenses.api.repository.TripRepository
@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val vehicleRepository: VehicleRepository,
    private val tripRepository: TripRepository
) {

    // --- Функція для зміни імені ---
    @Transactional
    fun updateUserName(userId: Long, request: UpdateNameRequest): User {
        val user = findUserById(userId)

        if (user.name == request.newName) {
            return user
        }

        val updatedUser = user.copy(name = request.newName)
        return userRepository.save(updatedUser)
    }

    @Transactional
    fun updateUserPassword(userId: Long, request: UpdatePasswordRequest) {
        val user = findUserById(userId)

        if (!passwordEncoder.matches(request.oldPassword, user.passwordHash)) {
            throw IllegalArgumentException("Неправильний старий пароль")
        }

        val newHashedPassword = passwordEncoder.encode(request.newPassword)

        val updatedUser = user.copy(passwordHash = newHashedPassword)
        userRepository.save(updatedUser)
    }

    @Transactional
    fun deleteUserAccount(userId: Long) {
        if (!userRepository.existsById(userId)) {
            throw IllegalArgumentException("Користувача з ID $userId не знайдено")
        }
        val user = findUserById(userId)

        tripRepository.deleteAllByUserId(user.id!!)
        vehicleRepository.deleteAllByUserId(user.id!!)

        userRepository.deleteById(userId)
    }

    private fun findUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Користувача з ID $userId не знайдено") }
    }
}