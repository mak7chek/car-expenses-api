package com.mak7chek.carexpenses.api.service

import com.mak7chek.carexpenses.api.dto.AuthRequest
import com.mak7chek.carexpenses.api.dto.AuthResponse
import com.mak7chek.carexpenses.api.model.User
import com.mak7chek.carexpenses.api.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    fun register(request: AuthRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email ${request.email} вже використовується")
        }

        // 2. Хешуємо пароль
        val hashedPassword = passwordEncoder.encode(request.password)

        val newUser = User(
            name = request.name,
            email = request.email,
            passwordHash = hashedPassword

        )

        val savedUser = userRepository.save(newUser)
        val token = jwtService.generateToken(savedUser)
        // 5. Повертаємо відповідь
        return AuthResponse(
            userId = savedUser.id!!,
            email = savedUser.email,
            message = "Реєстрація успішна",
            token = token
        )
    }

    fun login(request: AuthRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("Користувача з таким email не знайдено") }

        // 2. Перевіряємо пароль
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Неправильний пароль")
        }
        val token = jwtService.generateToken(user)
        // 3. Повертаємо відповідь
        return AuthResponse(
            userId = user.id!!,
            email = user.email,
            message = "Вхід успішний",
            token = token
        )
    }
}