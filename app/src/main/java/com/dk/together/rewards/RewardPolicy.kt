package com.dk.together.rewards

data class RewardRule(
    val grant: RewardGrant,
    val dailyCap: Int? = null,
) {
    init {
        require(dailyCap == null || dailyCap > 0) { "dailyCap must be positive when supplied" }
    }
}

class RewardPolicy(
    private val rules: Map<RewardEventType, RewardRule>,
) {
    fun ruleFor(type: RewardEventType): RewardRule = rules[type]
        ?: error("No reward rule configured for $type")

    companion object {
        /**
         * v0.1 balance values are deliberately provisional. The architecture is the important part;
         * these numbers can be tuned later without migrations or UI rewrites.
         *
         * General app activity awards Evelune XP. Direct pet care additionally awards Pet Bond XP
         * and Pet Coins. Chat/note caps prevent trivial spam farming while still rewarding normal use.
         */
        fun default(): RewardPolicy = RewardPolicy(
            mapOf(
                RewardEventType.QUESTION_ANSWER to RewardRule(RewardGrant(eveluneXp = 25)),
                RewardEventType.QUESTION_CHAT_MESSAGE to RewardRule(RewardGrant(eveluneXp = 3), dailyCap = 20),
                RewardEventType.DAILY_CARD_COMPLETE to RewardRule(RewardGrant(eveluneXp = 25)),
                RewardEventType.DAILY_CHALLENGE_COMPLETE to RewardRule(RewardGrant(eveluneXp = 30)),
                RewardEventType.DAILY_GAME_COMPLETE to RewardRule(RewardGrant(eveluneXp = 20)),
                RewardEventType.STICKY_NOTE_SENT to RewardRule(RewardGrant(eveluneXp = 5), dailyCap = 10),
                RewardEventType.MEMORY_CREATED to RewardRule(RewardGrant(eveluneXp = 20), dailyCap = 5),
                RewardEventType.CONVERSATION_MESSAGE to RewardRule(RewardGrant(eveluneXp = 2), dailyCap = 20),
                RewardEventType.PET_FEED to RewardRule(RewardGrant(eveluneXp = 8, petBondXp = 12, petCoins = 5)),
                RewardEventType.PET_CLEAN to RewardRule(RewardGrant(eveluneXp = 10, petBondXp = 15, petCoins = 6)),
                RewardEventType.PET_PLAY to RewardRule(RewardGrant(eveluneXp = 8, petBondXp = 12, petCoins = 5)),
                RewardEventType.PET_HATCH to RewardRule(RewardGrant(eveluneXp = 20, petBondXp = 5, petCoins = 3)),
                RewardEventType.PET_INTERACTION to RewardRule(RewardGrant(eveluneXp = 4, petBondXp = 5, petCoins = 2)),
            )
        )
    }
}
