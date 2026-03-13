package com.jlsh.aifit.core.ui.components.inputs

import java.time.LocalDate
import java.time.Period

/**
 * Pure functions for date validation and age calculation.
 * Fully decoupled from UI — designed for unit testing.
 */
object DateValidator {

    const val MIN_AGE = 13
    const val MAX_AGE = 120

    /**
     * Returns the number of days in a given month/year, handling leap years.
     */
    fun daysInMonth(year: Int, month: Int): Int {
        require(month in 1..12) { "Month must be 1..12, was $month" }
        return LocalDate.of(year, month, 1).lengthOfMonth()
    }

    /**
     * Returns true if [year] is a leap year.
     */
    fun isLeapYear(year: Int): Boolean = LocalDate.of(year, 1, 1).isLeapYear

    /**
     * Calculates the age in full years from [birthDate] to [referenceDate].
     * Returns null if [birthDate] is after [referenceDate].
     */
    fun calculateAge(birthDate: LocalDate, referenceDate: LocalDate = LocalDate.now()): Int? {
        if (birthDate.isAfter(referenceDate)) return null
        return Period.between(birthDate, referenceDate).years
    }

    /**
     * Validates a birth date against business rules.
     * Returns a [DateValidationResult] indicating success or the specific failure.
     */
    fun validate(
        birthDate: LocalDate,
        referenceDate: LocalDate = LocalDate.now(),
    ): DateValidationResult {
        if (birthDate.isAfter(referenceDate)) {
            return DateValidationResult.FutureDate
        }
        val age = calculateAge(birthDate, referenceDate) ?: return DateValidationResult.FutureDate
        if (age < MIN_AGE) {
            return DateValidationResult.TooYoung(MIN_AGE)
        }
        if (age > MAX_AGE) {
            return DateValidationResult.TooOld(MAX_AGE)
        }
        return DateValidationResult.Valid
    }

    /**
     * Formats a date as ISO-8601 (yyyy-MM-dd).
     */
    fun toIsoString(year: Int, month: Int, day: Int): String {
        val clampedDay = day.coerceIn(1, daysInMonth(year, month))
        return LocalDate.of(year, month, clampedDay).toString()
    }

    /**
     * Parses an ISO-8601 date string. Returns null on failure.
     */
    fun parseIsoString(value: String): LocalDate? =
        runCatching { LocalDate.parse(value) }.getOrNull()

    /**
     * Clamps day to valid range for the given month/year.
     */
    fun clampDay(day: Int, year: Int, month: Int): Int =
        day.coerceIn(1, daysInMonth(year, month))

    /**
     * Returns the earliest allowed birth date based on [MAX_AGE].
     */
    fun earliestAllowedDate(referenceDate: LocalDate = LocalDate.now()): LocalDate =
        referenceDate.minusYears(MAX_AGE.toLong())

    /**
     * Returns the latest allowed birth date based on [MIN_AGE].
     */
    fun latestAllowedDate(referenceDate: LocalDate = LocalDate.now()): LocalDate =
        referenceDate.minusYears(MIN_AGE.toLong())
}

/**
 * Sealed result type for date validation outcomes.
 */
sealed class DateValidationResult {
    data object Valid : DateValidationResult()
    data object FutureDate : DateValidationResult()
    data class TooYoung(val minAge: Int) : DateValidationResult()
    data class TooOld(val maxAge: Int) : DateValidationResult()

    fun toErrorMessage(): String? = when (this) {
        is Valid -> null
        is FutureDate -> "La fecha no puede ser futura"
        is TooYoung -> "Debes tener al menos $minAge años"
        is TooOld -> "La edad máxima es $maxAge años"
    }
}

