// У вашому Spring Boot проекті
package com.mak7chek.carexpenses.api.dto

import java.time.LocalDate

data class StatisticsRequest(
    val dateFrom: LocalDate,
    val dateTo: LocalDate,
    val vehicleIds: List<Long>?
)

/**
 * Головна відповідь зі статистикою.
 */
data class StatisticsResponse(
    val totalExpenses: Double,
    val expensesByVehicle: List<VehicleExpenseStat>,
    val expensesOverTime: List<TimeExpenseStat>
)

data class VehicleExpenseStat(
    val vehicleId: Long,
    val vehicleName: String,
    val totalCost: Double
)

data class TimeExpenseStat(
    val period: LocalDate,
    val totalCost: Double
)