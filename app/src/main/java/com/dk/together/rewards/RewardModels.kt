package com.dk.together.rewards

enum class RewardEventType {
    QUESTION_ANSWER,
    QUESTION_CHAT_MESSAGE,
    DAILY_CARD_COMPLETE,
    DAILY_CHALLENGE_COMPLETE,
    DAILY_GAME_COMPLETE,
    STICKY_NOTE_SENT,
    MEMORY_CREATED,
    CONVERSATION_MESSAGE,
    PET_FEED,
    PET_CLEAN,
    PET_PLAY,
    PET_HATCH,
    PET_INTERACTION,
}

data class RewardEvent(
    val id: String,
    val type: RewardEventType,
    val actorKey: String? = null,
    val petId: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val meaningful: Boolean = true,
) {
    init {
        require(id.isNotBlank()) { "Reward event id must not be blank" }
    }
}

data class RewardGrant(
    val eveluneXp: Int = 0,
    val petBondXp: Int = 0,
    val petCoins: Int = 0,
) {
    init {
        require(eveluneXp >= 0) { "Evelune XP cannot be negative" }
        require(petBondXp >= 0) { "Pet Bond XP cannot be negative" }
        require(petCoins >= 0) { "Pet Coins cannot be negative" }
    }

    companion object {
        val NONE = RewardGrant()
    }
}

data class RewardBalance(
    val eveluneXp: Int = 0,
    val petCoins: Int = 0,
)

data class LevelProgress(
    val level: Int,
    val totalXp: Int,
    val xpIntoLevel: Int,
    val xpForNextLevel: Int,
)

data class PetBondProgress(
    val petId: String,
    val bondXp: Int,
    val progress: LevelProgress,
)

enum class RewardDecision {
    GRANTED,
    DUPLICATE,
    DAILY_CAP_REACHED,
    NOT_MEANINGFUL,
}

data class RewardLedgerEntry(
    val eventId: String,
    val eventType: RewardEventType,
    val actorKey: String?,
    val petId: String?,
    val decision: RewardDecision,
    val grant: RewardGrant,
    val createdAtEpochMs: Long,
)

data class RewardResult(
    val decision: RewardDecision,
    val grant: RewardGrant,
    val balanceAfter: RewardBalance,
    val globalProgressAfter: LevelProgress,
    val petProgressAfter: PetBondProgress?,
    val globalLevelsGained: Int = 0,
    val petLevelsGained: Int = 0,
) {
    val globalLevelUp: Boolean get() = globalLevelsGained > 0
    val petLevelUp: Boolean get() = petLevelsGained > 0
}

object RewardProgression {
    const val DEFAULT_BASE_XP = 100
    const val DEFAULT_GROWTH_XP = 25

    /**
     * Provisional progression curve. Balance values are intentionally centralized so they can be
     * tuned later without changing event/persistence logic.
     */
    fun progress(
        totalXp: Int,
        baseXp: Int = DEFAULT_BASE_XP,
        growthXp: Int = DEFAULT_GROWTH_XP,
        maxLevel: Int = 999,
    ): LevelProgress {
        require(totalXp >= 0)
        require(baseXp > 0)
        require(growthXp >= 0)
        require(maxLevel >= 1)

        var level = 1
        var remaining = totalXp
        var needed = baseXp
        while (level < maxLevel && remaining >= needed) {
            remaining -= needed
            level += 1
            needed += growthXp
        }
        return LevelProgress(
            level = level,
            totalXp = totalXp,
            xpIntoLevel = remaining,
            xpForNextLevel = if (level >= maxLevel) 0 else needed,
        )
    }
}
