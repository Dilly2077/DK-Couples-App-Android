package com.dk.together

import com.dk.together.content.EveluneContentBank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentBankTest {
    @Test
    fun banksContainTwoYearsOfDailyContent() {
        assertEquals(730, EveluneContentBank.questions.size)
        assertEquals(730, EveluneContentBank.cardGames.size)
        assertEquals(730, EveluneContentBank.challenges.size)
        assertTrue(EveluneContentBank.cardGames.all { it.items.size == 4 })
    }

    @Test
    fun contentAvoidsExPartnerAndComfortPrefaces() {
        val text = buildList {
            addAll(EveluneContentBank.questions.map { it.prompt })
            addAll(EveluneContentBank.challenges.map { it.prompt })
            addAll(EveluneContentBank.cardGames.flatMap { it.items }.map { it.prompt })
        }.joinToString("\n").lowercase()
        assertFalse(text.contains("ex-partner"))
        assertFalse(text.contains("ex partner"))
        assertFalse(text.contains("if you're comfortable"))
        assertFalse(text.contains("if you are comfortable"))
    }

    @Test
    fun cardScoringKeepsMeanAndDifferenceSeparate() {
        val first = listOf(1, 2, 3, 4)
        val second = listOf(2, 2, 4, 4)
        assertEquals(2.75, EveluneContentBank.scoreMean(first, second), 0.001)
        assertEquals(0.5, EveluneContentBank.averageDifference(first, second), 0.001)
    }
}
