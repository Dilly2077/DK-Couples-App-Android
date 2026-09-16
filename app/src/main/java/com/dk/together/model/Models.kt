package com.dk.together.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class CoupleProfile(
    val youName: String = "",
    val partnerName: String = "",
    val startEpochDay: Long = LocalDate.now().toEpochDay(),
    val onboarded: Boolean = false
)

// Kept for the later shared-pet phase. The pet UI is intentionally out of the core flow for now.
data class PetStats(
    val hunger: Int = 72,
    val happiness: Int = 82,
    val cleanliness: Int = 78,
    val energy: Int = 70,
    val affection: Int = 86
)

data class AppPreferences(
    val profile: CoupleProfile = CoupleProfile(),
    val mood: String = "",
    val status: String = "",
    val partnerMood: String = "",
    val partnerStatus: String = "",
    val widgetNote: String = "",
    val hearts: Int = 0,
    val petName: String = "Nova",
    val pet: PetStats = PetStats(),
    val petRoom: String = "Living room",
    val petLastUpdatedMs: Long = System.currentTimeMillis(),
    val demoAsPartner: Boolean = false
)

data class QuestionContent(
    val id: String,
    val category: String,
    val prompt: String
)

data class CardContent(
    val id: String,
    val deck: String,
    val prompt: String
)

data class GamePrompt(
    val id: String,
    val category: String,
    val optionA: String,
    val optionB: String
)

data class DateIdea(
    val title: String,
    val category: String,
    val cost: String
)

object RelationshipMath {
    fun daysTogether(startEpochDay: Long, nowEpochDay: Long = LocalDate.now().toEpochDay()): Long =
        (nowEpochDay - startEpochDay).coerceAtLeast(0)

    fun estimatedHeartbeats(days: Long, bpm: Int = 70): Long =
        days.coerceAtLeast(0) * 24L * 60L * bpm.coerceIn(30, 220)

    fun relationshipBreakdown(startEpochDay: Long, today: LocalDate = LocalDate.now()): Triple<Int, Int, Int> {
        val start = LocalDate.ofEpochDay(startEpochDay)
        if (start.isAfter(today)) return Triple(0, 0, 0)
        var cursor = start
        var years = 0
        while (!cursor.plusYears(1).isAfter(today)) {
            cursor = cursor.plusYears(1)
            years++
        }
        var months = 0
        while (!cursor.plusMonths(1).isAfter(today)) {
            cursor = cursor.plusMonths(1)
            months++
        }
        val days = ChronoUnit.DAYS.between(cursor, today).toInt()
        return Triple(years, months, days)
    }
}
