package com.mak7chek.carexpenses.api.service

import com.mak7chek.carexpenses.api.dto.StatisticsRequest
import com.mak7chek.carexpenses.api.dto.StatisticsResponse
import com.mak7chek.carexpenses.api.repository.TripRepository
import com.mak7chek.carexpenses.api.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime

@Service
class StatisticsService(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getStatistics(userEmail: String, request: StatisticsRequest): StatisticsResponse {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { UsernameNotFoundException("Користувача не знайдено") }

        val startDateTime = request.dateFrom.atStartOfDay()
        val endDateTime = request.dateTo.atTime(LocalTime.MAX)

        val vehicleIds = request.vehicleIds?.ifEmpty { null }

        val total = tripRepository.getSumTotalCostByUserAndPeriod(user, startDateTime, endDateTime, vehicleIds)
        val byVehicle = tripRepository.getExpensesByVehicle(user, startDateTime, endDateTime, vehicleIds)
        val overTime = tripRepository.getExpensesOverTime(user, startDateTime, endDateTime, vehicleIds)

        return StatisticsResponse(
            totalExpenses = total,
            expensesByVehicle = byVehicle,
            expensesOverTime = overTime
        )
    }
}