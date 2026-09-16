package com.dk.together.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class CoupleProfile(
    val youName: String = "",
    val partnerName: String = "",
    val startEpochDay: Long = LocalDate.now().toEpochDay(),
    val onboarded: Boolean = false
)

data class PetStats(
    val hunger: Int = 72,
    val happiness: Int = 82,
    val cleanliness: Int = 78,
    val energy: Int = 70,
    val affection: Int = 86
)

data class AppPreferences(
    val profile: CoupleProfile = CoupleProfile(),
    val mood: String = "Calm",
    val status: String = "Free",
    val partnerMood: String = "Loved",
    val partnerStatus: String = "Thinking of you",
    val widgetNote: String = "Thinking of you 💜",
    val hearts: Int = 120,
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

data class DateIdea(
    val title: String,
    val category: String,
    val cost: String
)

object PetMath {
    fun decayed(stats: PetStats, lastUpdatedMs: Long, nowMs: Long = System.currentTimeMillis()): PetStats {
        val elapsedHours = ((nowMs - lastUpdatedMs).coerceAtLeast(0L) / 3_600_000L).toInt()
        if (elapsedHours == 0) return stats
        return stats.copy(
            hunger = (stats.hunger - elapsedHours * 2).coerceIn(0, 100),
            happiness = (stats.happiness - elapsedHours / 2).coerceIn(0, 100),
            cleanliness = (stats.cleanliness - elapsedHours).coerceIn(0, 100),
            energy = (stats.energy - elapsedHours).coerceIn(0, 100),
            affection = (stats.affection - elapsedHours / 4).coerceIn(0, 100)
        )
    }

    fun thought(stats: PetStats): String = when {
        stats.hunger < 35 -> "Could I have a snack? 🍓"
        stats.cleanliness < 35 -> "I feel a little grubby… 🫧"
        stats.energy < 30 -> "Tiny nap, please? 💤"
        stats.happiness < 40 -> "Will someone play with me? 🧸"
        stats.affection < 40 -> "Cuddle time? 💜"
        stats.hunger < 60 -> "The kitchen smells good…"
        stats.cleanliness < 60 -> "Maybe bath time soon?"
        else -> "I like it when we're all here together ✨"
    }
}

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
