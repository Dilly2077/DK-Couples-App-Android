package com.dk.together.pets.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HatchEngineTest {
    private val duration = 1_000L
    private val engine = HatchEngine(HatchDurationProvider { duration })

    @Test
    fun stageIsDerivedFromClockWithoutBackgroundCountdown() {
        val session = engine.createSession("h1", PetSpecies.BUNNY, 10_000L)

        assertEquals(HatchStage.PRISTINE, engine.snapshot(session, 10_000L).stage)
        assertEquals(HatchStage.WOBBLE, engine.snapshot(session, 10_350L).stage)
        assertEquals(HatchStage.SMALL_CRACK, engine.snapshot(session, 10_550L).stage)
        assertEquals(HatchStage.MEDIUM_CRACK, engine.snapshot(session, 10_700L).stage)
        assertEquals(HatchStage.HEAVY_CRACK, engine.snapshot(session, 10_800L).stage)
        assertEquals(HatchStage.SPLIT_SHELL, engine.snapshot(session, 10_900L).stage)
        assertEquals(HatchStage.HATCH_FLASH, engine.snapshot(session, 10_970L).stage)
    }

    @Test
    fun completedSessionBecomesReadyToClaim() {
        val session = engine.createSession("h1", PetSpecies.DRAGON, 0L)
        val snapshot = engine.snapshot(session, 1_000L)

        assertTrue(snapshot.complete)
        assertFalse(snapshot.claimed)
        assertEquals(HatchStage.READY_TO_CLAIM, snapshot.stage)
        assertEquals(0L, snapshot.remainingMs)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotClaimBeforeTimerCompletes() {
        val session = engine.createSession("h1", PetSpecies.FOX, 0L)
        engine.claim(session, 999L)
    }

    @Test
    fun claimedSessionIsMarkedClaimed() {
        val session = engine.createSession("h1", PetSpecies.FOX, 0L)
        val claimed = engine.claim(session, 1_000L)
        val snapshot = engine.snapshot(claimed, 1_500L)

        assertTrue(snapshot.complete)
        assertTrue(snapshot.claimed)
        assertEquals(HatchStage.CLAIMED, snapshot.stage)
    }
}
