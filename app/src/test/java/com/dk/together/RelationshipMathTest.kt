package com.dk.together

import com.dk.together.model.ContentBanks
import com.dk.together.model.QuestionContent
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
    fun expandedQuestionBankIsLargeAndVaried() {
        val base = listOf(QuestionContent("q1", "Communication", "How are we doing?"))
        val expanded = ContentBanks.expandedQuestions(base)
        assertTrue(expanded.size > 700)
        assertTrue(expanded.any { it.category == "Sex & Pleasure" && it.prompt.contains("orgasm", ignoreCase = true) })
        assertTrue(expanded.any { it.category == "Conflict" })
        assertTrue(expanded.any { it.category == "Future" })
    }

    @Test
    fun gameBankHasEnoughFiveRoundVariety() {
        val games = ContentBanks.games()
        assertTrue(games.size >= 100)
        assertTrue(games.map { it.category }.distinct().size >= 8)
    }
}
