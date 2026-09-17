package com.dk.together.pets.play

import com.dk.together.pets.engine.InMemoryPetEngineRepository
import com.dk.together.pets.engine.PetEngine
import com.dk.together.pets.engine.PetInstance
import com.dk.together.pets.engine.PetRoom
import com.dk.together.pets.engine.PetSpecies
import com.dk.together.rewards.InMemoryRewardRepository
import com.dk.together.rewards.RewardDecision
import com.dk.together.rewards.RewardEngine
import com.dk.together.rewards.RewardHooks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayEngineTest {

    private data class Fixture(
        val petRepository: InMemoryPetEngineRepository,
        val petEngine: PetEngine,
        val playRepository: InMemoryPlayRepository,
        val rewardRepository: InMemoryRewardRepository,
        val engine: PlayEngine,
    )

    private fun fixture(room: PetRoom = PetRoom.PLAYROOM, cooldownMs: Long = 30L * 60L * 1000L): Fixture {
        val petRepository = InMemoryPetEngineRepository()
        petRepository.savePet(
            PetInstance(
                id = "pet-1",
                species = PetSpecies.PUPPY,
                nickname = "Mocha",
                hatchedAtEpochMs = 0L,
                lastNeedsUpdateEpochMs = 1_000L,
                room = room,
            )
        )
        val petEngine = PetEngine(petRepository)
        val playRepository = InMemoryPlayRepository()
        val rewardRepository = InMemoryRewardRepository()
        val engine = PlayEngine(
            petEngine = petEngine,
            repository = playRepository,
            rewardHooks = RewardHooks(RewardEngine(rewardRepository)),
            cooldownMs = cooldownMs,
        )
        return Fixture(petRepository, petEngine, playRepository, rewardRepository, engine)
    }

    @Test
    fun `all four toy trackers require their intended gestures`() {
        val ball = PlayProgressTracker(PlayToy.BALL)
        repeat(5) { ball.register(PlayGesture.TAP) }
        assertTrue(ball.complete)

        val plush = PlayProgressTracker(PlayToy.PLUSH)
        repeat(4) { plush.register(PlayGesture.TAP) }
        assertTrue(plush.complete)

        val frisbee = PlayProgressTracker(PlayToy.FRISBEE)
        repeat(4) { frisbee.register(PlayGesture.SWIPE_UP) }
        assertTrue(frisbee.complete)

        val rope = PlayProgressTracker(PlayToy.ROPE)
        repeat(3) {
            rope.register(PlayGesture.SWIPE_LEFT)
            rope.register(PlayGesture.SWIPE_RIGHT)
        }
        assertTrue(rope.complete)
    }

    @Test
    fun `rope ignores repeated same-direction pulls`() {
        val rope = PlayProgressTracker(PlayToy.ROPE)
        repeat(6) { rope.register(PlayGesture.SWIPE_LEFT) }
        assertEquals(1, rope.completedCount)
        assertFalse(rope.complete)
    }

    @Test
    fun `completed play grants canonical rewards and happy state`() {
        val f = fixture(cooldownMs = 0L)
        val start = f.engine.start("pet-1", PlayToy.BALL, "play-1", nowEpochMs = 1_000L)
        assertEquals(PlayStartDecision.Started, start.decision)

        val result = f.engine.complete("pet-1", "play-1", activityComplete = true, nowEpochMs = 2_000L)

        assertEquals(PlayCompleteDecision.Completed, result.decision)
        assertEquals(RewardDecision.GRANTED, result.reward?.decision)
        assertEquals(8, result.reward?.grant?.eveluneXp)
        assertEquals(12, result.reward?.grant?.petBondXp)
        assertEquals(5, result.reward?.grant?.petCoins)
        assertEquals("HAPPY", result.pet?.behaviour?.name)
    }

    @Test
    fun `incomplete activity cannot grant rewards`() {
        val f = fixture(cooldownMs = 0L)
        f.engine.start("pet-1", PlayToy.PLUSH, "play-incomplete", nowEpochMs = 1_000L)

        val result = f.engine.complete("pet-1", "play-incomplete", activityComplete = false, nowEpochMs = 2_000L)

        assertEquals(PlayCompleteDecision.ActivityIncomplete, result.decision)
        assertEquals(0, f.rewardRepository.loadBalance().eveluneXp)
        assertEquals(PlayReceiptStatus.STARTED, f.playRepository.receipt("play-incomplete")?.status)
    }

    @Test
    fun `play completion starts per-pet cooldown`() {
        val f = fixture(cooldownMs = 30L * 60L * 1000L)
        f.engine.start("pet-1", PlayToy.BALL, "play-a", nowEpochMs = 1_000L)
        f.engine.complete("pet-1", "play-a", activityComplete = true, nowEpochMs = 2_000L)

        val blocked = f.engine.start("pet-1", PlayToy.FRISBEE, "play-b", nowEpochMs = 3_000L)
        assertTrue(blocked.decision is PlayStartDecision.CoolingDown)
        assertEquals(8, f.rewardRepository.loadBalance().eveluneXp)
    }

    @Test
    fun `cooldown expires without changing reward policy`() {
        val cooldown = 10_000L
        val f = fixture(cooldownMs = cooldown)
        f.engine.start("pet-1", PlayToy.BALL, "play-a", nowEpochMs = 1_000L)
        f.engine.complete("pet-1", "play-a", activityComplete = true, nowEpochMs = 2_000L)

        val allowed = f.engine.start("pet-1", PlayToy.ROPE, "play-b", nowEpochMs = 12_000L)
        assertEquals(PlayStartDecision.Started, allowed.decision)
    }

    @Test
    fun `same completed interaction cannot reward twice`() {
        val f = fixture(cooldownMs = 0L)
        f.engine.start("pet-1", PlayToy.BALL, "play-retry", nowEpochMs = 1_000L)
        val first = f.engine.complete("pet-1", "play-retry", activityComplete = true, nowEpochMs = 2_000L)
        val second = f.engine.complete("pet-1", "play-retry", activityComplete = true, nowEpochMs = 3_000L)

        assertEquals(PlayCompleteDecision.Completed, first.decision)
        assertEquals(PlayCompleteDecision.AlreadyCompleted, second.decision)
        assertEquals(8, f.rewardRepository.loadBalance().eveluneXp)
        assertEquals(5, f.rewardRepository.loadBalance().petCoins)
    }

    @Test
    fun `started interaction can recover after pet mutation interruption`() {
        val f = fixture(cooldownMs = 0L)
        f.engine.start("pet-1", PlayToy.ROPE, "play-recover", nowEpochMs = 1_000L)
        f.petEngine.playOnce("pet-1", "play-recover", nowEpochMs = 2_000L)

        val recovered = f.engine.complete("pet-1", "play-recover", activityComplete = true, nowEpochMs = 3_000L)

        assertEquals(PlayCompleteDecision.Completed, recovered.decision)
        assertEquals(RewardDecision.GRANTED, recovered.reward?.decision)
        assertEquals(PlayReceiptStatus.COMPLETED, f.playRepository.receipt("play-recover")?.status)
    }

    @Test
    fun `pet must be in playroom`() {
        val f = fixture(room = PetRoom.GARDEN)

        val result = f.engine.start("pet-1", PlayToy.BALL, "wrong-room", nowEpochMs = 1_000L)

        assertEquals(PlayStartDecision.PetNotInPlayroom, result.decision)
        assertEquals(0, f.rewardRepository.loadBalance().petCoins)
    }
}
