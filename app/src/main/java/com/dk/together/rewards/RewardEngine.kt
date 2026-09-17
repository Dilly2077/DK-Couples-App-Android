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
        return resultFor(decision, grant, event.petId)
    }

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

    private fun duplicateResult(event: RewardEvent): RewardResult = resultFor(
        decision = RewardDecision.DUPLICATE,
        grant = RewardGrant.NONE,
        petId = event.petId,
    )

    private fun resultFor(
        decision: RewardDecision,
        grant: RewardGrant,
        petId: String?,
    ): RewardResult {
        val balance = repository.loadBalance()
        return RewardResult(
            decision = decision,
            grant = grant,
            balanceAfter = balance,
            globalProgressAfter = RewardProgression.progress(balance.eveluneXp),
            petProgressAfter = petId?.let { petProgress(it) },
        )
    }
}
