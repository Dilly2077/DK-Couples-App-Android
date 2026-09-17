package com.dk.together.rewards

import java.time.Instant
import java.time.ZoneId

class RewardEngine(
    private val repository: RewardRepository,
    private val policy: RewardPolicy = RewardPolicy.default(),
    private val dayStartProvider: (Long) -> Long = { epochMs ->
        Instant.ofEpochMilli(epochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    },
) {
    fun reward(event: RewardEvent): RewardResult {
        if (repository.hasEvent(event.id)) return duplicateResult(event)

        val rule = policy.ruleFor(event.type)
        if (rule.grant.petBondXp > 0) {
            require(!event.petId.isNullOrBlank()) { "${event.type} requires petId because it awards Pet Bond XP" }
        }

        val balanceBefore = repository.loadBalance()
        val globalLevelBefore = RewardProgression.progress(balanceBefore.eveluneXp).level
        val petLevelBefore = event.petId?.let {
            RewardProgression.progress(repository.loadPetBondXp(it)).level
        }

        val decision: RewardDecision
        val grant: RewardGrant
        when {
            !event.meaningful -> {
                decision = RewardDecision.NOT_MEANINGFUL
                grant = RewardGrant.NONE
            }
            rule.dailyCap != null && repository.countGrantedEvents(
                type = event.type,
                actorKey = event.actorKey,
                petId = event.petId,
                sinceEpochMs = dayStartProvider(event.createdAtEpochMs),
            ) >= rule.dailyCap -> {
                decision = RewardDecision.DAILY_CAP_REACHED
                grant = RewardGrant.NONE
            }
            else -> {
                decision = RewardDecision.GRANTED
                grant = rule.grant
            }
        }

        val entry = RewardLedgerEntry(
            eventId = event.id,
            eventType = event.type,
            actorKey = event.actorKey,
            petId = event.petId,
            decision = decision,
            grant = grant,
            createdAtEpochMs = event.createdAtEpochMs,
        )

        if (!repository.record(entry)) return duplicateResult(event)

        val balanceAfter = repository.loadBalance()
        val globalProgressAfter = RewardProgression.progress(balanceAfter.eveluneXp)
        val petProgressAfter = event.petId?.let { petProgress(it) }
        return RewardResult(
            decision = decision,
            grant = grant,
            balanceAfter = balanceAfter,
            globalProgressAfter = globalProgressAfter,
            petProgressAfter = petProgressAfter,
            globalLevelsGained = (globalProgressAfter.level - globalLevelBefore).coerceAtLeast(0),
            petLevelsGained = if (petLevelBefore == null || petProgressAfter == null) 0 else {
                (petProgressAfter.progress.level - petLevelBefore).coerceAtLeast(0)
            },
        )
    }

    fun spendCoins(
        spendId: String,
        amount: Int,
        reason: String,
        createdAtEpochMs: Long = System.currentTimeMillis(),
    ): CoinSpendResult = repository.spendCoins(spendId, amount, reason, createdAtEpochMs)

    fun balance(): RewardBalance = repository.loadBalance()

    fun globalProgress(): LevelProgress = RewardProgression.progress(repository.loadBalance().eveluneXp)

    fun petProgress(petId: String): PetBondProgress {
        val xp = repository.loadPetBondXp(petId)
        return PetBondProgress(
            petId = petId,
            bondXp = xp,
            progress = RewardProgression.progress(xp),
        )
    }

    fun recentRewards(limit: Int = 100): List<RewardLedgerEntry> = repository.recent(limit)

    private fun duplicateResult(event: RewardEvent): RewardResult {
        val balance = repository.loadBalance()
        return RewardResult(
            decision = RewardDecision.DUPLICATE,
            grant = RewardGrant.NONE,
            balanceAfter = balance,
            globalProgressAfter = RewardProgression.progress(balance.eveluneXp),
            petProgressAfter = event.petId?.let { petProgress(it) },
        )
    }
}
