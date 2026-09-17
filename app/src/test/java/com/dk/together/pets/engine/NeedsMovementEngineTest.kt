package com.dk.together.pets.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NeedsMovementEngineTest {
    @Test
    fun hiddenNeedsIncreaseWithElapsedTimeAndExposeOnlyNeedState() {
        val engine = PetNeedsEngine(
            NeedTuning(
                hungerGainPerHour = 10.0,
                dirtGainPerHour = 5.0,
                hungryThreshold = 20.0,
                dirtyThreshold = 20.0
            )
        )
        val pet = PetInstance(
            id = "pet-1",
            species = PetSpecies.OTTER,
            hatchedAtEpochMs = 0L,
            lastNeedsUpdateEpochMs = 0L
        )

        val advanced = engine.advance(pet, 3L * 3_600_000L)
        val snapshot = engine.snapshot(advanced)

        assertEquals(30.0, advanced.hunger, 0.001)
        assertEquals(15.0, advanced.dirtiness, 0.001)
        assertEquals(setOf(PetNeed.FOOD), snapshot.needs)
        assertEquals(PetNeed.FOOD, snapshot.primaryNeed)
        assertEquals(PetBehaviour.HUNGRY, snapshot.suggestedBehaviour)
    }

    @Test
    fun feedingAndCleaningReduceInternalNeeds() {
        val engine = PetNeedsEngine()
        val pet = PetInstance(
            id = "pet-1",
            species = PetSpecies.FOX,
            hatchedAtEpochMs = 0L,
            hunger = 90.0,
            dirtiness = 90.0,
            lastNeedsUpdateEpochMs = 0L
        )

        val fed = engine.feed(pet, 0L)
        val cleaned = engine.clean(fed, 0L)

        assertTrue(fed.hunger < 90.0)
        assertTrue(cleaned.dirtiness < 90.0)
        assertEquals(PetBehaviour.HAPPY, cleaned.behaviour)
    }

    @Test
    fun movementTargetStaysInsideNormalizedRoomBounds() {
        val engine = PetMovementEngine()
        val pet = PetInstance(
            id = "pet-1",
            species = PetSpecies.TIGER,
            hatchedAtEpochMs = 0L,
            position = NormalizedPosition(0.5, 0.7)
        )
        val random = SequenceRandom(listOf(0.0, 1.0 - 1e-9))

        val plan = engine.planNextMove(pet, random)

        assertTrue(plan.to.x in 0.10..0.90)
        assertTrue(plan.to.y in 0.55..0.88)
        assertEquals(FacingDirection.LEFT, plan.facing)
        assertTrue(plan.durationMs in 900L..7_000L)
    }

    private class SequenceRandom(values: List<Double>) : RandomSource {
        private val iterator = values.iterator()
        override fun nextUnitDouble(): Double = if (iterator.hasNext()) iterator.next() else 0.5
    }
}
