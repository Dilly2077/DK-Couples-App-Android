package com.dk.together.pets.feeding

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
import org.junit.Assert.assertNotNull
import org.junit.Test

class FeedingEngineTest {

    private data class Fixture(
        val petRepository: InMemoryPetEngineRepository,
        val feedingRepository: InMemoryFeedingRepository,
        val rewardRepository: InMemoryRewardRepository,
        val engine: FeedingEngine,
    )

    private fun fixture(
        hunger: Double = 80.0,
        room: PetRoom = PetRoom.KITCHEN,
    ): Fixture {
        val petRepository = InMemoryPetEngineRepository()
        petRepository.savePet(
            PetInstance(
                id = "pet-1",
                species = PetSpecies.PUPPY,
                nickname = "Mocha",
                hatchedAtEpochMs = 0L,
                lastNeedsUpdateEpochMs = 1_000L,
                room = room,
                hunger = hunger,
            )
        )
        val feedingRepository = InMemoryFeedingRepository()
        val rewardRepository = InMemoryRewardRepository()
        val engine = FeedingEngine(
            petEngine = PetEngine(petRepository),
            repository = feedingRepository,
            rewardHooks = RewardHooks(RewardEngine(rewardRepository)),
        )
        return Fixture(petRepository, feedingRepository, rewardRepository, engine)
    }

    @Test
    fun `starter inventory seeds only once`() {
        val f = fixture()
        f.engine.ensureStarterInventory()
        f.engine.ensureStarterInventory()

        assertEquals(3, f.engine.inventory()[FoodId.KIBBLE])
        assertEquals(1, f.engine.inventory()[FoodId.FISH])
        assertEquals(0, f.engine.inventory()[FoodId.PREMIUM_FEAST_TRAY])
    }

    @Test
    fun `feeding consumes one item reduces hunger and grants rewards`() {
        val f = fixture(hunger = 80.0)
        f.feedingRepository.add(FoodId.KIBBLE, 2)

        val result = f.engine.feed(
            petId = "pet-1",
            foodId = FoodId.KIBBLE,
            interactionId = "feed-1",
            actorKey = "you",
            nowEpochMs = 1_000L,
        )

        assertEquals(FeedDecision.Completed, result.decision)
        assertEquals(45.0, result.pet?.hunger ?: -1.0, 0.001)
        assertEquals(1, result.remainingQuantity)
        assertEquals(RewardDecision.GRANTED, result.reward?.decision)
        assertEquals(8, result.reward?.grant?.eveluneXp)
        assertEquals(12, result.reward?.grant?.petBondXp)
        assertEquals(5, result.reward?.grant?.petCoins)
    }

    @Test
    fun `retrying completed interaction does not consume food feed again or reward again`() {
        val f = fixture(hunger = 80.0)
        f.feedingRepository.add(FoodId.FISH, 2)

        val first = f.engine.feed("pet-1", FoodId.FISH, "feed-retry", nowEpochMs = 1_000L)
        val second = f.engine.feed("pet-1", FoodId.FISH, "feed-retry", nowEpochMs = 1_000L)

        assertEquals(FeedDecision.Completed, first.decision)
        assertEquals(FeedDecision.AlreadyCompleted, second.decision)
        assertEquals(35.0, f.petRepository.findPet("pet-1")?.hunger ?: -1.0, 0.001)
        assertEquals(1, f.feedingRepository.quantity(FoodId.FISH))
        assertEquals(8, f.rewardRepository.loadBalance().eveluneXp)
        assertEquals(5, f.rewardRepository.loadBalance().petCoins)
    }

    @Test
    fun `pet must be in kitchen`() {
        val f = fixture(room = PetRoom.GARDEN)
        f.feedingRepository.add(FoodId.KIBBLE, 1)

        val result = f.engine.feed("pet-1", FoodId.KIBBLE, "wrong-room", nowEpochMs = 1_000L)

        assertEquals(FeedDecision.PetNotInKitchen, result.decision)
        assertEquals(1, f.feedingRepository.quantity(FoodId.KIBBLE))
        assertEquals(0, f.rewardRepository.loadBalance().eveluneXp)
    }

    @Test
    fun `full pet cannot be farmed for food rewards`() {
        val f = fixture(hunger = 5.0)
        f.feedingRepository.add(FoodId.BISCUIT, 1)

        val result = f.engine.feed("pet-1", FoodId.BISCUIT, "too-full", nowEpochMs = 1_000L)

        assertEquals(FeedDecision.PetNotHungryEnough, result.decision)
        assertEquals(1, f.feedingRepository.quantity(FoodId.BISCUIT))
        assertEquals(0, f.rewardRepository.loadBalance().petCoins)
    }

    @Test
    fun `out of stock does not alter pet`() {
        val f = fixture(hunger = 80.0)

        val result = f.engine.feed("pet-1", FoodId.PREMIUM_FEAST_TRAY, "no-stock", nowEpochMs = 1_000L)

        assertEquals(FeedDecision.OutOfStock, result.decision)
        assertEquals(80.0, f.petRepository.findPet("pet-1")?.hunger ?: -1.0, 0.001)
        assertEquals(0, f.rewardRepository.loadBalance().petCoins)
    }

    @Test
    fun `reserved retry is safe after pet mutation`() {
        val f = fixture(hunger = 80.0)
        f.feedingRepository.add(FoodId.CARROT, 1)

        val reservation = f.feedingRepository.reserveFood(
            interactionId = "resume-1",
            petId = "pet-1",
            foodId = FoodId.CARROT,
            nowEpochMs = 1_000L,
        )
        assertEquals(ReserveFoodDecision.RESERVED, reservation.decision)

        val result = f.engine.feed("pet-1", FoodId.CARROT, "resume-1", nowEpochMs = 1_000L)
        val receipt = f.feedingRepository.receipt("resume-1")

        assertEquals(FeedDecision.Completed, result.decision)
        assertEquals(55.0, result.pet?.hunger ?: -1.0, 0.001)
        assertEquals(0, f.feedingRepository.quantity(FoodId.CARROT))
        assertNotNull(receipt)
        assertEquals(FeedingReceiptStatus.COMPLETED, receipt?.status)
    }
}
