package com.dk.together.rewards

/**
 * Canonical entry points for app features to emit reward events without duplicating event IDs or
 * knowing balance values. Future screens should call these hooks after the underlying action has
 * successfully completed.
 */
class RewardHooks(
    private val engine: RewardEngine,
) {
    fun questionAnswered(questionId: String, actorKey: String, meaningful: Boolean = true): RewardResult =
        engine.reward(
            RewardEvent(
                id = "question_answer:$questionId:$actorKey",
                type = RewardEventType.QUESTION_ANSWER,
                actorKey = actorKey,
                meaningful = meaningful,
            )
        )

    fun questionChatMessage(
        questionId: String,
        messageId: Long,
        actorKey: String,
        meaningful: Boolean = true,
    ): RewardResult = engine.reward(
        RewardEvent(
            id = "question_chat:$questionId:$messageId",
            type = RewardEventType.QUESTION_CHAT_MESSAGE,
            actorKey = actorKey,
            meaningful = meaningful,
        )
    )

    fun dailyCardCompleted(cardId: String, actorKey: String, meaningful: Boolean = true): RewardResult =
        engine.reward(
            RewardEvent(
                id = "daily_card:$cardId:$actorKey",
                type = RewardEventType.DAILY_CARD_COMPLETE,
                actorKey = actorKey,
                meaningful = meaningful,
            )
        )

    fun dailyChallengeCompleted(challengeId: String, actorKey: String, meaningful: Boolean = true): RewardResult =
        engine.reward(
            RewardEvent(
                id = "daily_challenge:$challengeId:$actorKey",
                type = RewardEventType.DAILY_CHALLENGE_COMPLETE,
                actorKey = actorKey,
                meaningful = meaningful,
            )
        )

    fun dailyGameCompleted(gameId: String, actorKey: String, meaningful: Boolean = true): RewardResult =
        engine.reward(
            RewardEvent(
                id = "daily_game:$gameId:$actorKey",
                type = RewardEventType.DAILY_GAME_COMPLETE,
                actorKey = actorKey,
                meaningful = meaningful,
            )
        )

    fun stickyNoteSent(noteId: String, actorKey: String, meaningful: Boolean = true): RewardResult =
        engine.reward(
            RewardEvent(
                id = "sticky_note:$noteId",
                type = RewardEventType.STICKY_NOTE_SENT,
                actorKey = actorKey,
                meaningful = meaningful,
            )
        )

    fun memoryCreated(memoryId: String, actorKey: String? = null, meaningful: Boolean = true): RewardResult =
        engine.reward(
            RewardEvent(
                id = "memory:$memoryId",
                type = RewardEventType.MEMORY_CREATED,
                actorKey = actorKey,
                meaningful = meaningful,
            )
        )

    fun conversationMessage(
        threadId: String,
        messageId: Long,
        actorKey: String,
        meaningful: Boolean = true,
    ): RewardResult = engine.reward(
        RewardEvent(
            id = "conversation:$threadId:$messageId",
            type = RewardEventType.CONVERSATION_MESSAGE,
            actorKey = actorKey,
            meaningful = meaningful,
        )
    )

    fun petFed(
        petId: String,
        interactionId: String,
        actorKey: String? = null,
        meaningful: Boolean = true,
    ): RewardResult = petReward(RewardEventType.PET_FEED, petId, interactionId, actorKey, meaningful)

    fun petCleaned(
        petId: String,
        interactionId: String,
        actorKey: String? = null,
        meaningful: Boolean = true,
    ): RewardResult = petReward(RewardEventType.PET_CLEAN, petId, interactionId, actorKey, meaningful)

    fun petPlayedWith(
        petId: String,
        interactionId: String,
        actorKey: String? = null,
        meaningful: Boolean = true,
    ): RewardResult = petReward(RewardEventType.PET_PLAY, petId, interactionId, actorKey, meaningful)

    fun petHatched(
        petId: String,
        hatchId: String,
        actorKey: String? = null,
    ): RewardResult = petReward(RewardEventType.PET_HATCH, petId, hatchId, actorKey, true)

    fun petInteraction(
        petId: String,
        interactionId: String,
        actorKey: String? = null,
        meaningful: Boolean = true,
    ): RewardResult = petReward(RewardEventType.PET_INTERACTION, petId, interactionId, actorKey, meaningful)

    fun balance(): RewardBalance = engine.balance()
    fun globalProgress(): LevelProgress = engine.globalProgress()
    fun petProgress(petId: String): PetBondProgress = engine.petProgress(petId)

    private fun petReward(
        type: RewardEventType,
        petId: String,
        interactionId: String,
        actorKey: String?,
        meaningful: Boolean,
    ): RewardResult = engine.reward(
        RewardEvent(
            id = "${type.name.lowercase()}:$petId:$interactionId",
            type = type,
            actorKey = actorKey,
            petId = petId,
            meaningful = meaningful,
        )
    )
}
