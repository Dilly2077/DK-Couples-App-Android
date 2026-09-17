package com.dk.together.rewards

interface RewardRepository {
    fun loadBalance(): RewardBalance
    fun loadPetBondXp(petId: String): Int
    fun hasEvent(eventId: String): Boolean
    fun countGrantedEvents(
        type: RewardEventType,
        actorKey: String?,
        petId: String?,
        sinceEpochMs: Long,
    ): Int

    /**
     * Atomically records the ledger entry and applies its grant. Returns false when eventId already
     * exists, which makes every reward action idempotent.
     */
    fun record(entry: RewardLedgerEntry): Boolean

    fun recent(limit: Int = 100): List<RewardLedgerEntry>
}

class InMemoryRewardRepository : RewardRepository {
    private var balance = RewardBalance()
    private val petXp = linkedMapOf<String, Int>()
    private val entries = linkedMapOf<String, RewardLedgerEntry>()

    override fun loadBalance(): RewardBalance = balance

    override fun loadPetBondXp(petId: String): Int = petXp[petId] ?: 0

    override fun hasEvent(eventId: String): Boolean = entries.containsKey(eventId)

    override fun countGrantedEvents(
        type: RewardEventType,
        actorKey: String?,
        petId: String?,
        sinceEpochMs: Long,
    ): Int = entries.values.count { entry ->
        entry.eventType == type &&
            entry.decision == RewardDecision.GRANTED &&
            entry.createdAtEpochMs >= sinceEpochMs &&
            (actorKey == null || entry.actorKey == actorKey) &&
            (petId == null || entry.petId == petId)
    }

    override fun record(entry: RewardLedgerEntry): Boolean {
        if (entries.containsKey(entry.eventId)) return false
        entries[entry.eventId] = entry
        balance = balance.copy(
            eveluneXp = balance.eveluneXp + entry.grant.eveluneXp,
            petCoins = balance.petCoins + entry.grant.petCoins,
        )
        val petId = entry.petId
        if (petId != null && entry.grant.petBondXp > 0) {
            petXp[petId] = (petXp[petId] ?: 0) + entry.grant.petBondXp
        }
        return true
    }

    override fun recent(limit: Int): List<RewardLedgerEntry> = entries.values
        .sortedByDescending { it.createdAtEpochMs }
        .take(limit.coerceAtLeast(0))
}
