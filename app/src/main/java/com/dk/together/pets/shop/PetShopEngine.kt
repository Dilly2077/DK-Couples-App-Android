package com.dk.together.pets.shop

import com.dk.together.pets.feeding.FeedingRepository
import com.dk.together.pets.feeding.FoodCatalog
import com.dk.together.pets.feeding.FoodDefinition
import com.dk.together.pets.feeding.FoodId
import com.dk.together.pets.feeding.InventoryCreditDecision
import com.dk.together.rewards.CoinSpendDecision
import com.dk.together.rewards.RewardBalance
import com.dk.together.rewards.RewardEngine

data class PetShopItem(
    val food: FoodDefinition,
    val coinCost: Int,
    val quantity: Int = 1,
) {
    init {
        require(coinCost > 0)
        require(quantity > 0)
    }
}

object PetShopCatalog {
    /** Provisional prices. Keep balancing here rather than scattering values through the UI. */
    val all: List<PetShopItem> = listOf(
        PetShopItem(FoodCatalog.get(FoodId.KIBBLE), coinCost = 8),
        PetShopItem(FoodCatalog.get(FoodId.FISH), coinCost = 12),
        PetShopItem(FoodCatalog.get(FoodId.CARROT), coinCost = 6),
        PetShopItem(FoodCatalog.get(FoodId.BERRY_BOWL), coinCost = 8),
        PetShopItem(FoodCatalog.get(FoodId.APPLE_SLICES), coinCost = 6),
        PetShopItem(FoodCatalog.get(FoodId.MILK_BOWL), coinCost = 8),
        PetShopItem(FoodCatalog.get(FoodId.BISCUIT), coinCost = 5),
        PetShopItem(FoodCatalog.get(FoodId.SALAD_BOWL), coinCost = 10),
        PetShopItem(FoodCatalog.get(FoodId.CUPCAKE_TREAT), coinCost = 8),
        PetShopItem(FoodCatalog.get(FoodId.PREMIUM_FEAST_TRAY), coinCost = 20),
    )

    private val byFood = all.associateBy { it.food.id }

    fun get(foodId: FoodId): PetShopItem = requireNotNull(byFood[foodId]) { "Unknown shop food: $foodId" }
}

enum class PurchaseDecision {
    PURCHASED,
    ALREADY_PURCHASED,
    INSUFFICIENT_COINS,
}

data class PurchaseResult(
    val decision: PurchaseDecision,
    val item: PetShopItem,
    val balanceAfter: RewardBalance,
    val inventoryQuantityAfter: Int,
)

/**
 * Pet Shop coordinator.
 *
 * Coin spending and inventory credit live in separate local databases, so the purchase uses the
 * same stable purchase id at both boundaries. If the app stops after coins are spent but before the
 * food is credited, retrying the purchase sees a duplicate spend and safely applies the missing
 * inventory credit exactly once.
 */
class PetShopEngine(
    private val rewards: RewardEngine,
    private val feedingRepository: FeedingRepository,
) {
    fun ensureInventoryInitialized() {
        feedingRepository.seedStarterInventoryIfEmpty(FoodCatalog.starterInventory)
    }

    fun catalog(): List<PetShopItem> = PetShopCatalog.all

    fun balance(): RewardBalance = rewards.balance()

    fun inventory(): Map<FoodId, Int> = feedingRepository.inventory()

    fun purchase(
        foodId: FoodId,
        purchaseId: String,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): PurchaseResult {
        require(purchaseId.isNotBlank())
        val item = PetShopCatalog.get(foodId)
        val transactionId = "pet_shop:$purchaseId"
        val spend = rewards.spendCoins(
            spendId = transactionId,
            amount = item.coinCost,
            reason = "FOOD:${foodId.name}",
            createdAtEpochMs = nowEpochMs,
        )

        if (spend.decision == CoinSpendDecision.INSUFFICIENT_FUNDS) {
            return PurchaseResult(
                decision = PurchaseDecision.INSUFFICIENT_COINS,
                item = item,
                balanceAfter = spend.balanceAfter,
                inventoryQuantityAfter = feedingRepository.quantity(foodId),
            )
        }

        val credit = feedingRepository.creditOnce(
            creditId = transactionId,
            foodId = foodId,
            quantity = item.quantity,
            createdAtEpochMs = nowEpochMs,
        )
        return PurchaseResult(
            decision = if (credit.decision == InventoryCreditDecision.CREDITED) {
                PurchaseDecision.PURCHASED
            } else {
                PurchaseDecision.ALREADY_PURCHASED
            },
            item = item,
            balanceAfter = spend.balanceAfter,
            inventoryQuantityAfter = credit.quantityAfter,
        )
    }
}
