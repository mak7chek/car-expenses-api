package com.mak7chek.carexpenses.api.repository

import com.mak7chek.carexpenses.api.dto.TimeExpenseStat
import com.mak7chek.carexpenses.api.dto.VehicleExpenseStat
import com.mak7chek.carexpenses.api.model.Trip
import com.mak7chek.carexpenses.api.model.User
import com.mak7chek.carexpenses.api.model.Vehicle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.time.LocalDateTime
@Repository
interface TripRepository : JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {

    fun findByUserId(userId: Long): List<Trip>
    fun deleteAllByUserId(userId: Long)

    @Query("SELECT t FROM Trip t JOIN FETCH t.user u JOIN FETCH t.vehicle v LEFT JOIN FETCH t.routePoints WHERE t.id = :id")
    fun findTripWithDetails(@Param("id") id: Long): Optional<Trip>

    fun findByUserAndCalculatedTotalCostIsNullAndEndTimeIsNotNull(user: User): List<Trip>

    /**
     * Розраховує загальну суму витрат для користувача за певний період.
     */
    @Query("SELECT COALESCE(SUM(t.calculatedTotalCost), 0.0) " +
            "FROM Trip t " +
            "WHERE t.user = :user " +
            "AND t.endTime BETWEEN :start AND :end " +
            "AND (:vehicleIds IS NULL OR t.vehicle.id IN :vehicleIds)")
    fun getSumTotalCostByUserAndPeriod(
        user: User,
        start: LocalDateTime,
        end: LocalDateTime,
        vehicleIds: List<Long>?
    ): Double

    /**
     * Групує витрати по авто
     */
    @Query("SELECT new com.mak7chek.carexpenses.api.dto.VehicleExpenseStat(t.vehicle.id, t.vehicle.name, SUM(t.calculatedTotalCost)) " +
            "FROM Trip t " +
            "WHERE t.user = :user " +
            "AND t.endTime BETWEEN :start AND :end " +
            "AND (:vehicleIds IS NULL OR t.vehicle.id IN :vehicleIds) " +
            "GROUP BY t.vehicle.id, t.vehicle.name " +
            "ORDER BY SUM(t.calculatedTotalCost) DESC")
    fun getExpensesByVehicle(
        user: User,
        start: LocalDateTime,
        end: LocalDateTime,
        vehicleIds: List<Long>?
    ): List<VehicleExpenseStat>

    /**
     * Групує витрати ПО ДНЯХ
     */
    @Query("SELECT new com.mak7chek.carexpenses.api.dto.TimeExpenseStat(CAST(t.endTime AS LocalDate), SUM(t.calculatedTotalCost)) " +
            "FROM Trip t " +
            "WHERE t.user = :user " +
            "AND t.endTime BETWEEN :start AND :end " +
            "AND (:vehicleIds IS NULL OR t.vehicle.id IN :vehicleIds) " +
            "GROUP BY CAST(t.endTime AS LocalDate) " +
            "ORDER BY CAST(t.endTime AS LocalDate) ASC")
    fun getExpensesOverTime(
        user: User,
        start: LocalDateTime,
        end: LocalDateTime,
        vehicleIds: List<Long>?
    ): List<TimeExpenseStat>
}