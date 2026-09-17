package com.dk.together.pets.play

import com.dk.together.pets.engine.InteractionDropTarget
import com.dk.together.pets.engine.PetEngine
import com.dk.together.pets.engine.PetInstance
import com.dk.together.pets.engine.PetRoom
import com.dk.together.rewards.RewardDecision
import com.dk.together.rewards.RewardHooks
import com.dk.together.rewards.RewardResult

enum class PlayToy(
    val target: InteractionDropTarget,
    val displayName: String,
    val assetKey: String,
) {
    BALL(InteractionDropTarget.PLAY_BALL, "Ball", "toy_ball"),
    ROPE(InteractionDropTarget.PLAY_ROPE, "Rope", "toy_rope"),
    PLUSH(InteractionDropTarget.PLAY_PLUSH, "Plush", "toy_plush"),
    FRISBEE(InteractionDropTarget.PLAY_FRISBEE, "Frisbee", "toy_frisbee"),
    ;

    companion object {
        fun fromTarget(target: InteractionDropTarget): PlayToy? = entries.firstOrNull { it.target == target }
    }
}

enum class PlayGesture {
    TAP,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    SWIPE_UP,
}

/**
 * Deterministic mini-game progress. Progress is intentionally activity-specific while remaining
 * presentation-agnostic so the production sprite animations can replace temporary Compose art.
 */
class PlayProgressTracker(val toy: PlayToy) {
    private var progressCount: Int = 0
    private var lastRopeDirection: PlayGesture? = null

    val requiredCount: Int = when (toy) {
        PlayToy.BALL -> 5
        PlayToy.ROPE -> 6
        PlayToy.PLUSH -> 4
        PlayToy.FRISBEE -> 4
    }

    val completedCount: Int get() = progressCount
    val progress: Double get() = (progressCount.toDouble() / requiredCount.toDouble()).coerceIn(0.0, 1.0)
    val complete: Boolean get() = progressCount >= requiredCount

    fun register(gesture: PlayGesture): Double {
        if (complete) return 1.0
        when (toy) {
            PlayToy.BALL -> if (gesture == PlayGesture.TAP) progressCount += 1
            PlayToy.PLUSH -> if (gesture == PlayGesture.TAP) progressCount += 1
            PlayToy.FRISBEE -> if (gesture == PlayGesture.SWIPE_UP) progressCount += 1
            PlayToy.ROPE -> {
                if (gesture == PlayGesture.SWIPE_LEFT || gesture == PlayGesture.SWIPE_RIGHT) {
                    if (lastRopeDirection == null || lastRopeDirection != gesture) {
                        progressCount += 1
                        lastRopeDirection = gesture
                    }
                }
            }
        }
        return progress
    }
}

enum class PlayReceiptStatus { STARTED, COMPLETED }

data class PlayReceipt(
    val interactionId: String,
    val petId: String,
    val toy: PlayToy,
    val status: PlayReceiptStatus,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long? = null,
)

interface PlayRepository {
    fun receipt(interactionId: String): PlayReceipt?
    fun startInteraction(interactionId: String, petId: String, toy: PlayToy, nowEpochMs: Long): PlayReceipt
    fun markCompleted(interactionId: String, nowEpochMs: Long)
    fun lastCompletedAt(petId: String): Long?
}

class InMemoryPlayRepository : PlayRepository {
    private val receipts = linkedMapOf<String, PlayReceipt>()
    private val lastCompleted = linkedMapOf<String, Long>()

    override fun receipt(interactionId: String): PlayReceipt? = receipts[interactionId]

    override fun startInteraction(interactionId: String, petId: String, toy: PlayToy, nowEpochMs: Long): PlayReceipt {
        return receipts.getOrPut(interactionId) {
            PlayReceipt(interactionId, petId, toy, PlayReceiptStatus.STARTED, nowEpochMs)
        }
    }

    override fun markCompleted(interactionId: String, nowEpochMs: Long) {
        val current = requireNotNull(receipts[interactionId]) { "Unknown play interaction: $interactionId" }
        if (current.status == PlayReceiptStatus.COMPLETED) return
        receipts[interactionId] = current.copy(status = PlayReceiptStatus.COMPLETED, completedAtEpochMs = nowEpochMs)
        lastCompleted[current.petId] = nowEpochMs
    }

    override fun lastCompletedAt(petId: String): Long? = lastCompleted[petId]
}

sealed interface PlayStartDecision {
    data object Started : PlayStartDecision
    data object Resumed : PlayStartDecision
    data object AlreadyCompleted : PlayStartDecision
    data object PetNotFound : PlayStartDecision
    data object PetNotInPlayroom : PlayStartDecision
    data class CoolingDown(val remainingMs: Long) : PlayStartDecision
}

data class PlayStartResult(
    val decision: PlayStartDecision,
    val pet: PetInstance?,
    val receipt: PlayReceipt? = null,
)

sealed interface PlayCompleteDecision {
    data object Completed : PlayCompleteDecision
    data object AlreadyCompleted : PlayCompleteDecision
    data object InteractionNotStarted : PlayCompleteDecision
    data object PetNotFound : PlayCompleteDecision
    data object PetNotInPlayroom : PlayCompleteDecision
    data object ActivityIncomplete : PlayCompleteDecision
}

data class PlayCompleteResult(
    val decision: PlayCompleteDecision,
    val pet: PetInstance?,
    val reward: RewardResult? = null,
)

/** Coordinates toy mini-games, hidden anti-farming cooldown, pet state, and rewards. */
class PlayEngine(
    private val petEngine: PetEngine,
    private val repository: PlayRepository,
    private val rewardHooks: RewardHooks,
    private val cooldownMs: Long = DEFAULT_COOLDOWN_MS,
) {
    init {
        require(cooldownMs >= 0L)
    }

    fun start(
        petId: String,
        toy: PlayToy,
        interactionId: String,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): PlayStartResult {
        require(interactionId.isNotBlank())

        val existing = repository.receipt(interactionId)
        if (existing != null) {
            val pet = petEngine.pet(petId, nowEpochMs)
            val decision = if (existing.status == PlayReceiptStatus.COMPLETED) {
                PlayStartDecision.AlreadyCompleted
            } else {
                PlayStartDecision.Resumed
            }
            return PlayStartResult(decision, pet, existing)
        }

        val pet = petEngine.pet(petId, nowEpochMs)
            ?: return PlayStartResult(PlayStartDecision.PetNotFound, null)
        if (pet.room != PetRoom.PLAYROOM) {
            return PlayStartResult(PlayStartDecision.PetNotInPlayroom, pet)
        }

        val lastCompleted = repository.lastCompletedAt(petId)
        if (lastCompleted != null) {
            val remaining = cooldownMs - (nowEpochMs - lastCompleted)
            if (remaining > 0L) {
                return PlayStartResult(PlayStartDecision.CoolingDown(remaining), pet)
            }
        }

        val receipt = repository.startInteraction(interactionId, petId, toy, nowEpochMs)
        return PlayStartResult(PlayStartDecision.Started, pet, receipt)
    }

    fun complete(
        petId: String,
        interactionId: String,
        activityComplete: Boolean,
        actorKey: String? = null,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): PlayCompleteResult {
        require(interactionId.isNotBlank())
        val receipt = repository.receipt(interactionId)
            ?: return PlayCompleteResult(PlayCompleteDecision.InteractionNotStarted, petEngine.pet(petId, nowEpochMs))

        val pet = petEngine.pet(petId, nowEpochMs)
            ?: return PlayCompleteResult(PlayCompleteDecision.PetNotFound, null)
        if (pet.room != PetRoom.PLAYROOM) {
            return PlayCompleteResult(PlayCompleteDecision.PetNotInPlayroom, pet)
        }

        if (receipt.status == PlayReceiptStatus.COMPLETED) {
            return PlayCompleteResult(PlayCompleteDecision.AlreadyCompleted, pet)
        }
        if (!activityComplete) {
            return PlayCompleteResult(PlayCompleteDecision.ActivityIncomplete, pet)
        }

        val care = petEngine.playOnce(
            id = petId,
            interactionId = interactionId,
            nowEpochMs = nowEpochMs,
        )
        val reward = rewardHooks.petPlayedWith(
            petId = petId,
            interactionId = interactionId,
            actorKey = actorKey,
            meaningful = true,
        )
        repository.markCompleted(interactionId, nowEpochMs)

        val decision = if (!care.applied && reward.decision == RewardDecision.DUPLICATE) {
            PlayCompleteDecision.AlreadyCompleted
        } else {
            PlayCompleteDecision.Completed
        }
        return PlayCompleteResult(decision, care.pet, reward)
    }

    companion object {
        /** v0.1 balancing value; deliberately easy to retune later. */
        const val DEFAULT_COOLDOWN_MS: Long = 30L * 60L * 1000L
    }
}
