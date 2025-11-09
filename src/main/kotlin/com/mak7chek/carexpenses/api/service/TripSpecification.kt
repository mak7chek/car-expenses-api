package com.mak7chek.carexpenses.api.service

import com.mak7chek.carexpenses.api.model.Trip
import com.mak7chek.carexpenses.api.model.User
import com.mak7chek.carexpenses.api.model.Vehicle
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate

object TripSpecification {

    fun hasUser(user: User): Specification<Trip> {
        return Specification { root, query, builder ->
            builder.equal(root.get<User>("user"), user)
        }
    }

    fun containsNote(search: String?): Specification<Trip> {
        return Specification { root, query, builder ->
            if (search.isNullOrBlank()) {
                builder.conjunction()
            } else {
                builder.like(builder.lower(root.get("notes")), "%${search.lowercase()}%")
            }
        }
    }

    fun hasVehicle(vehicleId: Long?): Specification<Trip> {
        return Specification { root, query, builder ->
            if (vehicleId == null) {
                builder.conjunction()
            } else {
                builder.equal(root.get<Vehicle>("vehicle").get<Long>("id"), vehicleId)
            }
        }
    }

    // 4. Фільтр по даті
    fun isBetweenDates(from: LocalDate?, to: LocalDate?): Specification<Trip> {
        return Specification { root, query, builder ->
            val predicates = mutableListOf<Predicate>()

            if (from != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("startTime"), from.atStartOfDay()))
            }
            if (to != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("startTime"), to.plusDays(1).atStartOfDay()))
            }

            builder.and(*predicates.toTypedArray())
        }
    }
    fun isDistanceBetween(min: Double?, max: Double?): Specification<Trip> {
        return Specification { root, query, builder ->
            val predicates = mutableListOf<Predicate>()

            if (min != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("totalDistanceKm"), min))
            }
            if (max != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("totalDistanceKm"), max))
            }

            builder.and(*predicates.toTypedArray())
        }
    }
}