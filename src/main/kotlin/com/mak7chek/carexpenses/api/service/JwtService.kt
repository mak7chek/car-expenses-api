package com.mak7chek.carexpenses.api.service

import com.mak7chek.carexpenses.api.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}")
    private val secretKeyString: String
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKeyString.toByteArray())
    }

    private val expirationTimeMs: Long = 1000 * 60 * 60 * 10

    // --- ГОЛОВНІ МЕТОДИ ---

    // 3. Генерація токена для користувача
    fun generateToken(user: User): String {
        val now = Date()
        val expiration = Date(now.time + expirationTimeMs)

        return Jwts.builder()
            .subject(user.email)
            .claim("userId", user.id)
            .claim("name", user.name)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun getEmailFromToken(token: String): String {
        return getAllClaimsFromToken(token).subject
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            val claims = getAllClaimsFromToken(token)
            val expiration = claims.expiration
            !expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }
}