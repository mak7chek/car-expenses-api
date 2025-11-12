package com.mak7chek.carexpenses.api.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import kotlin.math.sin

class HaversineCalculatorTest {

    // Вказуємо, що це тестовий метод
    @Test
    fun `calculateDistance - повинен коректно рахувати дистанцію між двома точками`() {

        // --- 1. Given ---
        // Ми беремо відомі координати, наприклад, Лондон і Париж.
        // Очікувана дистанція між ними ~344 км.
        val londonLat = 51.5074
        val londonLon = -0.1278
        val parisLat = 48.8566
        val parisLon = 2.3522

        // --- 2. When  ---
        val distance = HaversineCalculator.calculateDistance(
            lat1 = londonLat, lon1 = londonLon,
            lat2 = parisLat, lon2 = parisLon
        )
        // --- 3. Then  ---
        val expectedDistanceKm = 344.0
        val tolerance = Offset.offset(1.0) // Дозволена похибка +- 1 км

        assertThat(distance).isCloseTo(expectedDistanceKm, tolerance)
    }

    @Test
    fun `calculateDistance - повинен повертати 0, якщо точки однакові`() {
        // Given
        val lat = 50.4501
        val lon = 30.5234

        // When
        val distance = HaversineCalculator.calculateDistance(lat, lon, lat, lon)

        // Then
        assertThat(distance).isEqualTo(0.0)
    }
}