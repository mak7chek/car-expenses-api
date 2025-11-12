package com.mak7chek.carexpenses.api.controller

import com.mak7chek.carexpenses.api.dto.StatisticsRequest
import com.mak7chek.carexpenses.api.service.StatisticsService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statistics")
class StatisticsController(private val statisticsService: StatisticsService) {

    private fun getUserEmail(userDetails: UserDetails?): String {
        return userDetails?.username
            ?: throw AccessDeniedException("Доступ заборонено: не вдалося ідентифікувати користувача")
    }

    @PostMapping
    fun getStatistics(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody request: StatisticsRequest
    ): ResponseEntity<Any> {
        val userEmail = getUserEmail(userDetails)
        val stats = statisticsService.getStatistics(userEmail, request)
        return ResponseEntity.ok(stats)
    }
}