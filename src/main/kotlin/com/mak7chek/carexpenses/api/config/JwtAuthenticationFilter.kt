package com.mak7chek.carexpenses.api.config

import com.mak7chek.carexpenses.api.service.JwtService
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader: String? = req.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(req, res)
            return
        }

        val token = authHeader.substring("Bearer ".length)

        try {
            val userEmail = jwtService.getEmailFromToken(token)

            // Перевіряємо, чи юзер ще не автентифікований у цьому запиті
            if (SecurityContextHolder.getContext().authentication == null) {

                // 1. Завантажуємо юзера з бази
                val userDetails = this.userDetailsService.loadUserByUsername(userEmail)

                if (jwtService.isTokenValid(token, userDetails)) {
                    // 3. Створюємо "пропуск"
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(req)

                    // 4. Кладемо "пропуск" у кишеню Spring
                    SecurityContextHolder.getContext().authentication = authToken
                    logger.info("Authenticated user '$userEmail', setting security context")
                } else {
                    logger.warn("JWT token is valid but does not match user details.")
                }
            }
        } catch (e: ExpiredJwtException) {
            logger.warn("JWT token is expired: ${e.message}")
        } catch (e: SignatureException) {
            logger.warn("JWT signature is invalid: ${e.message}")
        } catch (e: MalformedJwtException) {
            logger.warn("JWT token is malformed: ${e.message}")
        } catch (e: Exception) {
            logger.error("Could not set user authentication in security context", e)
        }

        // 6. Завжди пропускаємо запит далі
        filterChain.doFilter(req, res)
    }
}