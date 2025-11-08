package com.mak7chek.carexpenses.api.config

import com.mak7chek.carexpenses.api.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class ApplicationConfig(
    private val userRepository: UserRepository
) {

    @Bean
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService { email ->
            val user = userRepository.findByEmail(email)
                .orElseThrow { UsernameNotFoundException("User not found with email: $email") }

            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

            org.springframework.security.core.userdetails.User(
                user.email,
                user.passwordHash,
                authorities
            )
        }
    }

    @Bean
    fun authenticationProvider(
        @Autowired userDetailsService: UserDetailsService,
        @Autowired passwordEncoder: PasswordEncoder
    ): AuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder)
        return authProvider
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }


}