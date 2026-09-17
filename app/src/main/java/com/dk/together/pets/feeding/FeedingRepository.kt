package com.dk.together.pets.feeding

enum class FeedingReceiptStatus {
    RESERVED,
    COMPLETED,
}

data class FeedingReceipt(
    val interactionId: String,
    val petId: String,
    val foodId: FoodId,
    val status: FeedingReceiptStatus,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long? = null,
)

enum class ReserveFoodDecision {
    RESERVED,
    ALREADY_RESERVED,
    OUT_OF_STOCK,
}

data class ReserveFoodResult(
    val decision: ReserveFoodDecision,
    val receipt: FeedingReceipt?,
    val remainingQuantity: Int,
)

interface FeedingRepository {
    fun seedStarterInventoryIfEmpty(starter: Map<FoodId, Int>)
    fun inventory(): Map<FoodId, Int>
    fun quantity(foodId: FoodId): Int
    fun add(foodId: FoodId, quantity: Int)
    fun receipt(interactionId: String): FeedingReceipt?

    /**
     * Atomically decrements one item and creates a RESERVED receipt. Retrying the same interaction
     * never consumes a second item.
     */
    fun reserveFood(
        interactionId: String,
        petId: String,
        foodId: FoodId,
        nowEpochMs: Long,
    ): ReserveFoodResult

    fun markCompleted(interactionId: String, completedAtEpochMs: Long): FeedingReceipt
}

class InMemoryFeedingRepository : FeedingRepository {
    private val stock = linkedMapOf<FoodId, Int>()
    private val receipts = linkedMapOf<String, FeedingReceipt>()

    override fun seedStarterInventoryIfEmpty(starter: Map<FoodId, Int>) {
        if (stock.isNotEmpty()) return
        starter.forEach { (food, amount) ->
            require(amount >= 0)
            if (amount > 0) stock[food] = amount
        }
    }

    override fun inventory(): Map<FoodId, Int> = FoodId.entries.associateWith { stock[it] ?: 0 }

    override fun quantity(foodId: FoodId): Int = stock[foodId] ?: 0

    override fun add(foodId: FoodId, quantity: Int) {
        require(quantity > 0)
        stock[foodId] = (stock[foodId] ?: 0) + quantity
    }

    override fun receipt(interactionId: String): FeedingReceipt? = receipts[interactionId]

    override fun reserveFood(
        interactionId: String,
        petId: String,
        foodId: FoodId,
        nowEpochMs: Long,
    ): ReserveFoodResult {
        require(interactionId.isNotBlank())
        require(petId.isNotBlank())

        val existing = receipts[interactionId]
        if (existing != null) {
            require(existing.petId == petId && existing.foodId == foodId) {
                "Interaction id already belongs to a different feeding action"
            }
            return ReserveFoodResult(
                decision = ReserveFoodDecision.ALREADY_RESERVED,
                receipt = existing,
                remainingQuantity = quantity(foodId),
            )
        }

        val current = quantity(foodId)
        if (current <= 0) {
            return ReserveFoodResult(ReserveFoodDecision.OUT_OF_STOCK, null, 0)
        }

        stock[foodId] = current - 1
        val receipt = FeedingReceipt(
            interactionId = interactionId,
            petId = petId,
            foodId = foodId,
            status = FeedingReceiptStatus.RESERVED,
            startedAtEpochMs = nowEpochMs,
        )
        receipts[interactionId] = receipt
        return ReserveFoodResult(
            decision = ReserveFoodDecision.RESERVED,
            receipt = receipt,
            remainingQuantity = current - 1,
        )
    }

    override fun markCompleted(interactionId: String, completedAtEpochMs: Long): FeedingReceipt {
        val existing = requireNotNull(receipts[interactionId]) { "Unknown feeding interaction: $interactionId" }
        if (existing.status == FeedingReceiptStatus.COMPLETED) return existing
        val completed = existing.copy(
            status = FeedingReceiptStatus.COMPLETED,
            completedAtEpochMs = completedAtEpochMs,
        )
        receipts[interactionId] = completed
        return completed
    }
}
