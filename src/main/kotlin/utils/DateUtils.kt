package utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

fun parseDate(dateStr: String?): LocalDate? {
    if (dateStr.isNullOrBlank()) return null
    return try { 
        LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: DateTimeParseException) {
        try { 
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e2: DateTimeParseException) { 
            null 
        }
    }
}

fun daysUntil(date: LocalDate?): Long? {
    if (date == null) return null
    return ChronoUnit.DAYS.between(LocalDate.now(), date)
}

fun formatDateFromYYYYMMDDToDDMMYYYY(dateStrYYYYMMDD: String?): String? = 
    parseDate(dateStrYYYYMMDD)?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))