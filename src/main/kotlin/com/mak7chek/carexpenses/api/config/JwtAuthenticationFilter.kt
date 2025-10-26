package com.mak7chek.carexpenses.api.config

import com.mak7chek.carexpenses.api.service.JwtService
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
class JwtAuthenticationFilter (
private val jwtService: JwtService,
private val userDetailsService: UserDetailsService
): OncePerRequestFilter()
{
    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        filterChain: FilterChain
    ){
        val authHeader: String? = req.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(req, res)
            return
        }

        val token = authHeader.substring("Bearer ".length)

        try {
            val userEmail = jwtService.getEmailFromToken(token)

            if (SecurityContextHolder.getContext().authentication == null) {
                val userDetails = this.userDetailsService.loadUserByUsername(userEmail)

                if (jwtService.isTokenValid(token)){
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(req)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        }catch (e:Exception){

        }
        filterChain.doFilter(req, res)
    }
}