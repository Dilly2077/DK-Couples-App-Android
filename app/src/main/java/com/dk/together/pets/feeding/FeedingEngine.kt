package com.dk.together.pets.feeding

import com.dk.together.pets.engine.PetEngine
import com.dk.together.pets.engine.PetInstance
import com.dk.together.pets.engine.PetRoom
import com.dk.together.rewards.RewardHooks
import com.dk.together.rewards.RewardResult

sealed interface FeedDecision {
    data object Completed : FeedDecision
    data object AlreadyCompleted : FeedDecision
    data object PetNotFound : FeedDecision
    data object PetNotInKitchen : FeedDecision
    data object PetNotHungryEnough : FeedDecision
    data object OutOfStock : FeedDecision
}

data class FeedingResult(
    val decision: FeedDecision,
    val pet: PetInstance?,
    val food: FoodDefinition,
    val remainingQuantity: Int,
    val reward: RewardResult? = null,
)

class FeedingEngine(
    private val petEngine: PetEngine,
    private val repository: FeedingRepository,
    private val rewardHooks: RewardHooks,
    private val minimumMeaningfulHunger: Double = 15.0,
) {
    init {
        require(minimumMeaningfulHunger in 0.0..100.0)
    }

    fun ensureStarterInventory() {
        repository.seedStarterInventoryIfEmpty(FoodCatalog.starterInventory)
    }

    fun inventory(): Map<FoodId, Int> = repository.inventory()

    /**
     * Completes one meaningful kitchen feeding. The same interaction id is safe to retry after a
     * process interruption: food reservation, pet hunger mutation, and reward emission are all
     * independently idempotent.
     */
    fun feed(
        petId: String,
        foodId: FoodId,
        interactionId: String,
        actorKey: String? = null,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): FeedingResult {
        require(interactionId.isNotBlank())
        val food = FoodCatalog.get(foodId)

        val existing = repository.receipt(interactionId)
        if (existing?.status == FeedingReceiptStatus.COMPLETED) {
            return FeedingResult(
                decision = FeedDecision.AlreadyCompleted,
                pet = petEngine.pet(petId, nowEpochMs),
                food = food,
                remainingQuantity = repository.quantity(foodId),
            )
        }

        val pet = petEngine.pet(petId, nowEpochMs)
            ?: return FeedingResult(FeedDecision.PetNotFound, null, food, repository.quantity(foodId))

        if (pet.room != PetRoom.KITCHEN) {
            return FeedingResult(FeedDecision.PetNotInKitchen, pet, food, repository.quantity(foodId))
        }

        if (existing == null && pet.hunger < minimumMeaningfulHunger) {
            return FeedingResult(
                FeedDecision.PetNotHungryEnough,
                pet,
                food,
                repository.quantity(foodId),
            )
        }

        val reservation = repository.reserveFood(
            interactionId = interactionId,
            petId = petId,
            foodId = foodId,
            nowEpochMs = nowEpochMs,
        )
        if (reservation.decision == ReserveFoodDecision.OUT_OF_STOCK) {
            return FeedingResult(FeedDecision.OutOfStock, pet, food, 0)
        }

        val care = petEngine.feedOnce(
            id = petId,
            interactionId = interactionId,
            hungerReduction = food.hungerReduction,
            nowEpochMs = nowEpochMs,
        )

        val reward = rewardHooks.petFed(
            petId = petId,
            interactionId = interactionId,
            actorKey = actorKey,
            meaningful = true,
        )
        repository.markCompleted(interactionId, nowEpochMs)

        return FeedingResult(
            decision = FeedDecision.Completed,
            pet = care.pet,
            food = food,
            remainingQuantity = repository.quantity(foodId),
            reward = reward,
        )
    }
}
