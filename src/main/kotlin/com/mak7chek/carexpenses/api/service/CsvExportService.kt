package com.mak7chek.carexpenses.api.service

import com.mak7chek.carexpenses.api.dto.TripResponse
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.springframework.stereotype.Service
import java.io.StringWriter

@Service
class CsvExportService {

    private val HEADERS = arrayOf(
        "ID Поїздки",
        "Автомобіль",
        "Нотатка",
        "Дата Початку",
        "Дата Кінця",
        "Дистанція (км)",
        "Витрачено (л)"
    )

    fun exportTripsToCsv(trips: List<TripResponse>): String {
        val stringWriter = StringWriter()

        val csvPrinter = CSVPrinter(stringWriter, CSVFormat.DEFAULT.builder()
            .setHeader(*HEADERS)
            .build())

        for (trip in trips) {
            csvPrinter.printRecord(
                trip.id,
                trip.vehicleName,
                trip.notes ?: "",
                trip.startTime,
                trip.endTime ?: "В дорозі",
                trip.totalDistanceKm,
                trip.totalFuelConsumedL
            )
        }


        csvPrinter.flush()
        csvPrinter.close()

        return stringWriter.toString()
    }
}