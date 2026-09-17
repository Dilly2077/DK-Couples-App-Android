package com.dk.together.pets.cleaning

import com.dk.together.pets.engine.NormalizedPosition
import com.dk.together.pets.engine.PetEngine
import com.dk.together.pets.engine.PetInstance
import com.dk.together.pets.engine.PetRoom
import com.dk.together.rewards.RewardDecision
import com.dk.together.rewards.RewardHooks
import com.dk.together.rewards.RewardResult
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot

sealed interface CleanDecision {
    data object Completed : CleanDecision
    data object AlreadyCompleted : CleanDecision
    data object PetNotFound : CleanDecision
    data object PetNotInGarden : CleanDecision
    data object PetNotDirtyEnough : CleanDecision
    data object CoverageIncomplete : CleanDecision
}

data class CleaningResult(
    val decision: CleanDecision,
    val pet: PetInstance?,
    val coverage: Double,
    val reward: RewardResult? = null,
)

/**
 * Small deterministic grid used by the close-up sponge interaction. Each scrub marks nearby cells
 * as clean, which gives the UI a stable 0..1 completion fraction without exposing pet dirtiness.
 */
class CleaningCoverageTracker(
    val columns: Int = 12,
    val rows: Int = 16,
    private val brushRadiusCells: Double = 1.35,
) {
    private val cleaned = linkedSetOf<Int>()

    init {
        require(columns >= 2)
        require(rows >= 2)
        require(brushRadiusCells > 0.0)
    }

    val cleanedCells: Set<Int> get() = cleaned.toSet()
    val totalCells: Int get() = columns * rows
    val coverage: Double get() = cleaned.size.toDouble() / totalCells.toDouble()

    fun scrub(position: NormalizedPosition): Double {
        val cx = position.x * (columns - 1)
        val cy = position.y * (rows - 1)
        val minX = floor(cx - brushRadiusCells).toInt().coerceAtLeast(0)
        val maxX = ceil(cx + brushRadiusCells).toInt().coerceAtMost(columns - 1)
        val minY = floor(cy - brushRadiusCells).toInt().coerceAtLeast(0)
        val maxY = ceil(cy + brushRadiusCells).toInt().coerceAtMost(rows - 1)

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                if (hypot(x - cx, y - cy) <= brushRadiusCells) {
                    cleaned += y * columns + x
                }
            }
        }
        return coverage
    }

    fun isClean(cellX: Int, cellY: Int): Boolean {
        require(cellX in 0 until columns)
        require(cellY in 0 until rows)
        return (cellY * columns + cellX) in cleaned
    }

    fun reset() {
        cleaned.clear()
    }
}

/** Coordinates the Garden hot-tub mini-game with hidden pet needs and the reward ledger. */
class CleaningEngine(
    private val petEngine: PetEngine,
    private val rewardHooks: RewardHooks,
    private val completionThreshold: Double = 0.85,
    private val minimumMeaningfulDirtiness: Double = 15.0,
) {
    init {
        require(completionThreshold in 0.5..1.0)
        require(minimumMeaningfulDirtiness in 0.0..100.0)
    }

    fun clean(
        petId: String,
        interactionId: String,
        coverage: Double,
        actorKey: String? = null,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): CleaningResult {
        require(interactionId.isNotBlank())
        require(coverage in 0.0..1.0)

        val pet = petEngine.pet(petId, nowEpochMs)
            ?: return CleaningResult(CleanDecision.PetNotFound, null, coverage)

        if (pet.room != PetRoom.GARDEN) {
            return CleaningResult(CleanDecision.PetNotInGarden, pet, coverage)
        }

        if (coverage < completionThreshold) {
            return CleaningResult(CleanDecision.CoverageIncomplete, pet, coverage)
        }

        if (pet.dirtiness < minimumMeaningfulDirtiness) {
            return CleaningResult(CleanDecision.PetNotDirtyEnough, pet, coverage)
        }

        val care = petEngine.cleanOnce(
            id = petId,
            interactionId = interactionId,
            nowEpochMs = nowEpochMs,
        )
        val reward = rewardHooks.petCleaned(
            petId = petId,
            interactionId = interactionId,
            actorKey = actorKey,
            meaningful = true,
        )

        val decision = if (!care.applied && reward.decision == RewardDecision.DUPLICATE) {
            CleanDecision.AlreadyCompleted
        } else {
            CleanDecision.Completed
        }

        return CleaningResult(
            decision = decision,
            pet = care.pet,
            coverage = coverage,
            reward = reward,
        )
    }
}
