package com.dk.together.pets.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PetEngineIntegrationTest {
    @Test
    fun completeHatchFlowCreatesPersistentPetAndUpdatesHistory() {
        val repo = InMemoryPetEngineRepository()
        val engine = PetEngine(
            repository = repo,
            random = FixedRandom(0.0),
            idSource = CountingIdSource(),
            hatchEngine = HatchEngine(HatchDurationProvider { 1_000L })
        )

        val start = engine.startHatch(10_000L)
        assertEquals(HatchStage.PRISTINE, start.stage)
        assertNotNull(repo.loadActiveHatch())

        val pet = engine.claimCompletedHatch(11_000L)

        assertEquals(PetSpecies.PUPPY, pet.species)
        assertEquals(1, engine.hatchHistory().totalClaimedHatches)
        assertTrue(PetSpecies.PUPPY in engine.hatchHistory().ownedSpecies)
        assertNull(repo.loadActiveHatch())
        assertEquals(1, repo.loadPets().size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun onlyOneEggCanIncubateAtATime() {
        val engine = PetEngine(
            repository = InMemoryPetEngineRepository(),
            random = FixedRandom(0.0),
            idSource = CountingIdSource(),
            hatchEngine = HatchEngine(HatchDurationProvider { 1_000L })
        )

        engine.startHatch(0L)
        engine.startHatch(1L)
    }

    @Test
    fun petCanMoveBetweenRoomsWithoutUiDependency() {
        val repo = InMemoryPetEngineRepository()
        val pet = PetInstance(
            id = "pet-1",
            species = PetSpecies.KITTEN,
            hatchedAtEpochMs = 0L
        )
        repo.savePet(pet)
        val engine = PetEngine(repository = repo, random = FixedRandom(0.5))

        val moved = engine.moveToRoom(
            id = "pet-1",
            room = PetRoom.KITCHEN,
            nowEpochMs = 1_000L
        )

        assertEquals(PetRoom.KITCHEN, moved.room)
        assertEquals(NormalizedPosition(0.5, 0.72), moved.position)
    }

    private class FixedRandom(private val value: Double) : RandomSource {
        override fun nextUnitDouble(): Double = value
    }

    private class CountingIdSource : IdSource {
        private var value = 0
        override fun nextId(prefix: String): String {
            value += 1
            return "$prefix-$value"
        }
    }
}
