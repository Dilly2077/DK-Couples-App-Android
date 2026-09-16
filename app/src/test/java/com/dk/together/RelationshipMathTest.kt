package com.dk.together

import com.dk.together.model.PetMath
import com.dk.together.model.PetStats
import com.dk.together.model.RelationshipMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RelationshipMathTest {
    @Test
    fun daysTogetherNeverNegative() {
        val today = LocalDate.of(2026, 9, 16).toEpochDay()
        assertEquals(0L, RelationshipMath.daysTogether(today + 10, today))
    }

    @Test
    fun heartbeatEstimateUsesConfiguredBpm() {
        assertEquals(100_800L, RelationshipMath.estimatedHeartbeats(1, 70))
    }

    @Test
    fun relationshipBreakdownWorksAcrossYearsAndMonths() {
        val start = LocalDate.of(2025, 1, 1).toEpochDay()
        assertEquals(Triple(1, 2, 4), RelationshipMath.relationshipBreakdown(start, LocalDate.of(2026, 3, 5)))
    }

    @Test
    fun petNeedsDecayOverElapsedHours() {
        val start = 1_000_000L
        val now = start + 5L * 3_600_000L
        val result = PetMath.decayed(PetStats(hunger = 80, cleanliness = 80), start, now)
        assertEquals(70, result.hunger)
        assertEquals(75, result.cleanliness)
    }

    @Test
    fun hungryPetAsksForFood() {
        val thought = PetMath.thought(PetStats(hunger = 20))
        assertTrue(thought.contains("snack", ignoreCase = true))
    }
}
