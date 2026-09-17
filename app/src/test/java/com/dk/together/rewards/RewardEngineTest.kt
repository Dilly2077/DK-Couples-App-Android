package com.dk.together.rewards

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardEngineTest {

    @Test
    fun `first meaningful event grants XP and duplicate does not`() {
        val repository = InMemoryRewardRepository()
        val engine = RewardEngine(repository)
        val event = RewardEvent(
            id = "question_answer:q1:you",
            type = RewardEventType.QUESTION_ANSWER,
            actorKey = "you",
            createdAtEpochMs = 1_000L,
        )

        val first = engine.reward(event)
        val second = engine.reward(event)

        assertEquals(RewardDecision.GRANTED, first.decision)
        assertEquals(25, first.grant.eveluneXp)
        assertEquals(25, first.balanceAfter.eveluneXp)
        assertEquals(RewardDecision.DUPLICATE, second.decision)
        assertEquals(0, second.grant.eveluneXp)
        assertEquals(25, second.balanceAfter.eveluneXp)
    }

    @Test
    fun `pet care awards global XP bond XP and pet coins`() {
        val repository = InMemoryRewardRepository()
        val engine = RewardEngine(repository)

        val result = engine.reward(
            RewardEvent(
                id = "pet_feed:pet-1:feed-1",
                type = RewardEventType.PET_FEED,
                actorKey = "you",
                petId = "pet-1",
                createdAtEpochMs = 2_000L,
            )
        )

        assertEquals(RewardDecision.GRANTED, result.decision)
        assertEquals(8, result.balanceAfter.eveluneXp)
        assertEquals(5, result.balanceAfter.petCoins)
        assertEquals(12, result.petProgressAfter?.bondXp)
    }

    @Test
    fun `pet bond XP levels independently from global progression`() {
        val repository = InMemoryRewardRepository()
        val policy = RewardPolicy(
            mapOf(
                RewardEventType.PET_PLAY to RewardRule(
                    RewardGrant(eveluneXp = 1, petBondXp = 60, petCoins = 1)
                )
            )
        )
        val engine = RewardEngine(repository, policy)

        engine.reward(RewardEvent("play-1", RewardEventType.PET_PLAY, petId = "pet-a"))
        val second = engine.reward(RewardEvent("play-2", RewardEventType.PET_PLAY, petId = "pet-a"))

        assertEquals(120, second.petProgressAfter?.bondXp)
        assertEquals(2, second.petProgressAfter?.progress?.level)
        assertEquals(20, second.petProgressAfter?.progress?.xpIntoLevel)
        assertEquals(125, second.petProgressAfter?.progress?.xpForNextLevel)
        assertEquals(1, second.globalProgressAfter.level)
    }

    @Test
    fun `non meaningful event is recorded but grants nothing`() {
        val repository = InMemoryRewardRepository()
        val engine = RewardEngine(repository)
        val event = RewardEvent(
            id = "pet_interaction:pet-1:no-op",
            type = RewardEventType.PET_INTERACTION,
            petId = "pet-1",
            meaningful = false,
        )

        val first = engine.reward(event)
        val second = engine.reward(event)

        assertEquals(RewardDecision.NOT_MEANINGFUL, first.decision)
        assertEquals(RewardGrant.NONE, first.grant)
        assertEquals(RewardDecision.DUPLICATE, second.decision)
        assertEquals(0, engine.balance().eveluneXp)
        assertEquals(0, engine.petProgress("pet-1").bondXp)
    }

    @Test
    fun `daily cap blocks spam and capped event remains idempotent`() {
        val repository = InMemoryRewardRepository()
        val policy = RewardPolicy(
            mapOf(
                RewardEventType.STICKY_NOTE_SENT to RewardRule(
                    grant = RewardGrant(eveluneXp = 5),
                    dailyCap = 2,
                )
            )
        )
        val engine = RewardEngine(repository, policy, dayStartProvider = { 0L })

        val first = engine.reward(RewardEvent("note-1", RewardEventType.STICKY_NOTE_SENT, actorKey = "you"))
        val second = engine.reward(RewardEvent("note-2", RewardEventType.STICKY_NOTE_SENT, actorKey = "you"))
        val capped = engine.reward(RewardEvent("note-3", RewardEventType.STICKY_NOTE_SENT, actorKey = "you"))
        val repeat = engine.reward(RewardEvent("note-3", RewardEventType.STICKY_NOTE_SENT, actorKey = "you"))

        assertEquals(RewardDecision.GRANTED, first.decision)
        assertEquals(RewardDecision.GRANTED, second.decision)
        assertEquals(RewardDecision.DAILY_CAP_REACHED, capped.decision)
        assertEquals(RewardDecision.DUPLICATE, repeat.decision)
        assertEquals(10, engine.balance().eveluneXp)
    }

    @Test
    fun `progression curve increases XP required per level`() {
        val atStart = RewardProgression.progress(0)
        val levelTwo = RewardProgression.progress(100)
        val levelThree = RewardProgression.progress(225)

        assertEquals(1, atStart.level)
        assertEquals(100, atStart.xpForNextLevel)
        assertEquals(2, levelTwo.level)
        assertEquals(125, levelTwo.xpForNextLevel)
        assertEquals(3, levelThree.level)
        assertEquals(150, levelThree.xpForNextLevel)
        assertTrue(levelThree.xpIntoLevel == 0)
    }
}
