package com.dk.together.pets.cleaning

import com.dk.together.pets.engine.InMemoryPetEngineRepository
import com.dk.together.pets.engine.NormalizedPosition
import com.dk.together.pets.engine.PetEngine
import com.dk.together.pets.engine.PetInstance
import com.dk.together.pets.engine.PetRoom
import com.dk.together.pets.engine.PetSpecies
import com.dk.together.rewards.InMemoryRewardRepository
import com.dk.together.rewards.RewardDecision
import com.dk.together.rewards.RewardEngine
import com.dk.together.rewards.RewardHooks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CleaningEngineTest {

    private data class Fixture(
        val pets: InMemoryPetEngineRepository,
        val petEngine: PetEngine,
        val rewards: InMemoryRewardRepository,
        val engine: CleaningEngine,
    )

    private fun fixture(
        dirtiness: Double = 90.0,
        room: PetRoom = PetRoom.GARDEN,
    ): Fixture {
        val pets = InMemoryPetEngineRepository()
        pets.savePet(
            PetInstance(
                id = "pet-1",
                species = PetSpecies.KITTEN,
                nickname = "Lumi",
                hatchedAtEpochMs = 0L,
                lastNeedsUpdateEpochMs = 1_000L,
                room = room,
                dirtiness = dirtiness,
            )
        )
        val petEngine = PetEngine(pets)
        val rewards = InMemoryRewardRepository()
        return Fixture(
            pets = pets,
            petEngine = petEngine,
            rewards = rewards,
            engine = CleaningEngine(
                petEngine = petEngine,
                rewardHooks = RewardHooks(RewardEngine(rewards)),
            ),
        )
    }

    @Test
    fun `scrub tracker accumulates deterministic coverage`() {
        val tracker = CleaningCoverageTracker(columns = 8, rows = 8, brushRadiusCells = 1.25)
        assertEquals(0.0, tracker.coverage, 0.0001)

        for (y in 0 until 8) {
            for (x in 0 until 8) {
                tracker.scrub(
                    NormalizedPosition(
                        x = x / 7.0,
                        y = y / 7.0,
                    )
                )
            }
        }

        assertEquals(1.0, tracker.coverage, 0.0001)
        assertTrue(tracker.cleanedCells.size == tracker.totalCells)
    }

    @Test
    fun `cleaning at threshold reduces dirt and grants clean rewards`() {
        val f = fixture(dirtiness = 90.0)

        val result = f.engine.clean(
            petId = "pet-1",
            interactionId = "clean-1",
            coverage = 0.85,
            actorKey = "you",
            nowEpochMs = 1_000L,
        )

        assertEquals(CleanDecision.Completed, result.decision)
        assertEquals(5.0, result.pet?.dirtiness ?: -1.0, 0.001)
        assertEquals(RewardDecision.GRANTED, result.reward?.decision)
        assertEquals(10, result.reward?.grant?.eveluneXp)
        assertEquals(15, result.reward?.grant?.petBondXp)
        assertEquals(6, result.reward?.grant?.petCoins)
    }

    @Test
    fun `retrying the same cleaning does not clean or reward twice`() {
        val f = fixture(dirtiness = 90.0)

        val first = f.engine.clean("pet-1", "clean-retry", 0.90, nowEpochMs = 1_000L)
        val second = f.engine.clean("pet-1", "clean-retry", 0.90, nowEpochMs = 1_000L)

        assertEquals(CleanDecision.Completed, first.decision)
        assertEquals(CleanDecision.AlreadyCompleted, second.decision)
        assertEquals(5.0, f.pets.findPet("pet-1")?.dirtiness ?: -1.0, 0.001)
        assertEquals(10, f.rewards.loadBalance().eveluneXp)
        assertEquals(6, f.rewards.loadBalance().petCoins)
    }

    @Test
    fun `retry after care mutation can recover missing reward exactly once`() {
        val f = fixture(dirtiness = 90.0)

        val care = f.petEngine.cleanOnce(
            id = "pet-1",
            interactionId = "recover-clean",
            nowEpochMs = 1_000L,
        )
        assertTrue(care.applied)
        assertEquals(5.0, care.pet.dirtiness, 0.001)
        assertEquals(0, f.rewards.loadBalance().eveluneXp)

        val recovered = f.engine.clean("pet-1", "recover-clean", 0.95, nowEpochMs = 1_000L)
        val retry = f.engine.clean("pet-1", "recover-clean", 0.95, nowEpochMs = 1_000L)

        assertEquals(CleanDecision.Completed, recovered.decision)
        assertEquals(RewardDecision.GRANTED, recovered.reward?.decision)
        assertEquals(CleanDecision.AlreadyCompleted, retry.decision)
        assertEquals(10, f.rewards.loadBalance().eveluneXp)
        assertEquals(6, f.rewards.loadBalance().petCoins)
    }

    @Test
    fun `coverage must reach completion threshold`() {
        val f = fixture(dirtiness = 90.0)

        val result = f.engine.clean("pet-1", "too-soon", 0.84, nowEpochMs = 1_000L)

        assertEquals(CleanDecision.CoverageIncomplete, result.decision)
        assertEquals(90.0, f.pets.findPet("pet-1")?.dirtiness ?: -1.0, 0.001)
        assertEquals(0, f.rewards.loadBalance().eveluneXp)
    }

    @Test
    fun `pet must be in garden`() {
        val f = fixture(dirtiness = 90.0, room = PetRoom.KITCHEN)

        val result = f.engine.clean("pet-1", "wrong-room", 0.95, nowEpochMs = 1_000L)

        assertEquals(CleanDecision.PetNotInGarden, result.decision)
        assertEquals(90.0, f.pets.findPet("pet-1")?.dirtiness ?: -1.0, 0.001)
        assertEquals(0, f.rewards.loadBalance().petCoins)
    }

    @Test
    fun `already clean pet cannot be farmed for rewards`() {
        val f = fixture(dirtiness = 5.0)

        val result = f.engine.clean("pet-1", "too-clean", 1.0, nowEpochMs = 1_000L)

        assertEquals(CleanDecision.PetNotDirtyEnough, result.decision)
        assertEquals(5.0, f.pets.findPet("pet-1")?.dirtiness ?: -1.0, 0.001)
        assertEquals(0, f.rewards.loadBalance().petCoins)
    }
}
