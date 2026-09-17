package com.dk.together.pets.shop

import com.dk.together.pets.feeding.FoodId
import com.dk.together.pets.feeding.InMemoryFeedingRepository
import com.dk.together.rewards.InMemoryRewardRepository
import com.dk.together.rewards.RewardDecision
import com.dk.together.rewards.RewardEngine
import com.dk.together.rewards.RewardEventType
import com.dk.together.rewards.RewardGrant
import com.dk.together.rewards.RewardLedgerEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PetShopEngineTest {

    private data class Fixture(
        val rewards: InMemoryRewardRepository,
        val feeding: InMemoryFeedingRepository,
        val rewardEngine: RewardEngine,
        val shop: PetShopEngine,
    )

    private fun fixture(coins: Int = 0): Fixture {
        val rewards = InMemoryRewardRepository()
        if (coins > 0) grantCoins(rewards, coins, "opening")
        val feeding = InMemoryFeedingRepository()
        val rewardEngine = RewardEngine(rewards)
        return Fixture(
            rewards = rewards,
            feeding = feeding,
            rewardEngine = rewardEngine,
            shop = PetShopEngine(rewardEngine, feeding),
        )
    }

    private fun grantCoins(repository: InMemoryRewardRepository, amount: Int, id: String) {
        repository.record(
            RewardLedgerEntry(
                eventId = "test-grant:$id",
                eventType = RewardEventType.PET_INTERACTION,
                actorKey = null,
                petId = null,
                decision = RewardDecision.GRANTED,
                grant = RewardGrant(petCoins = amount),
                createdAtEpochMs = 1_000L,
            )
        )
    }

    @Test
    fun `catalog contains every launch food once`() {
        val ids = PetShopCatalog.all.map { it.food.id }
        assertEquals(FoodId.entries.size, ids.size)
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(PetShopCatalog.all.all { it.coinCost > 0 && it.quantity > 0 })
    }

    @Test
    fun `purchase spends coins and credits kitchen inventory`() {
        val f = fixture(coins = 20)

        val result = f.shop.purchase(FoodId.KIBBLE, "buy-1", nowEpochMs = 2_000L)

        assertEquals(PurchaseDecision.PURCHASED, result.decision)
        assertEquals(12, result.balanceAfter.petCoins)
        assertEquals(1, f.feeding.quantity(FoodId.KIBBLE))
    }

    @Test
    fun `same purchase id never spends or credits twice`() {
        val f = fixture(coins = 30)

        val first = f.shop.purchase(FoodId.FISH, "same", nowEpochMs = 2_000L)
        val second = f.shop.purchase(FoodId.FISH, "same", nowEpochMs = 3_000L)

        assertEquals(PurchaseDecision.PURCHASED, first.decision)
        assertEquals(PurchaseDecision.ALREADY_PURCHASED, second.decision)
        assertEquals(18, f.rewards.loadBalance().petCoins)
        assertEquals(1, f.feeding.quantity(FoodId.FISH))
    }

    @Test
    fun `insufficient coins changes nothing and can be retried after earning`() {
        val f = fixture(coins = 3)

        val blocked = f.shop.purchase(FoodId.CARROT, "later", nowEpochMs = 2_000L)
        assertEquals(PurchaseDecision.INSUFFICIENT_COINS, blocked.decision)
        assertEquals(3, f.rewards.loadBalance().petCoins)
        assertEquals(0, f.feeding.quantity(FoodId.CARROT))

        grantCoins(f.rewards, 10, "later-top-up")
        val completed = f.shop.purchase(FoodId.CARROT, "later", nowEpochMs = 3_000L)
        assertEquals(PurchaseDecision.PURCHASED, completed.decision)
        assertEquals(7, f.rewards.loadBalance().petCoins)
        assertEquals(1, f.feeding.quantity(FoodId.CARROT))
    }

    @Test
    fun `retry recovers if coins were spent before inventory credit`() {
        val f = fixture(coins = 30)
        val item = PetShopCatalog.get(FoodId.SALAD_BOWL)
        f.rewardEngine.spendCoins(
            spendId = "pet_shop:recover",
            amount = item.coinCost,
            reason = "FOOD:${FoodId.SALAD_BOWL.name}",
            createdAtEpochMs = 2_000L,
        )

        assertEquals(20, f.rewards.loadBalance().petCoins)
        assertEquals(0, f.feeding.quantity(FoodId.SALAD_BOWL))

        val recovered = f.shop.purchase(FoodId.SALAD_BOWL, "recover", nowEpochMs = 3_000L)

        assertEquals(PurchaseDecision.PURCHASED, recovered.decision)
        assertEquals(20, f.rewards.loadBalance().petCoins)
        assertEquals(1, f.feeding.quantity(FoodId.SALAD_BOWL))
    }

    @Test
    fun `shop can initialize starter inventory before kitchen is opened`() {
        val f = fixture()
        f.shop.ensureInventoryInitialized()
        f.shop.ensureInventoryInitialized()

        assertEquals(3, f.feeding.quantity(FoodId.KIBBLE))
        assertEquals(1, f.feeding.quantity(FoodId.FISH))
        assertEquals(2, f.feeding.quantity(FoodId.BISCUIT))
    }
}
