package com.dk.together

import com.dk.together.model.RelationshipMath
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class RelationshipMathTest {
    @Test
    fun daysTogetherNeverNegative() {
        val today = LocalDate.of(2026, 9, 16).toEpochDay()
        assertEquals(0, RelationshipMath.daysTogether(today + 10, today))
    }

    @Test
    fun heartbeatEstimateUsesConfiguredBpm() {
        assertEquals(100_800, RelationshipMath.estimatedHeartbeats(1, 70))
    }

    @Test
    fun relationshipBreakdownWorksAcrossYearsAndMonths() {
        val start = LocalDate.of(2025, 1, 1).toEpochDay()
        assertEquals(Triple(1, 2, 4), RelationshipMath.relationshipBreakdown(start, LocalDate.of(2026, 3, 5)))
    }
}
