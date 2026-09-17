package com.dk.together.pets.engine

import kotlin.math.min

data class NeedTuning(
    val hungerGainPerHour: Double = 4.0,
    val dirtGainPerHour: Double = 2.0,
    val hungryThreshold: Double = 70.0,
    val dirtyThreshold: Double = 70.0,
    val feedReduction: Double = 70.0,
    val cleanReduction: Double = 85.0
) {
    init {
        require(hungerGainPerHour >= 0.0)
        require(dirtGainPerHour >= 0.0)
        require(hungryThreshold in 0.0..100.0)
        require(dirtyThreshold in 0.0..100.0)
        require(feedReduction in 0.0..100.0)
        require(cleanReduction in 0.0..100.0)
    }
}

data class PetNeedsSnapshot(
    val needs: Set<PetNeed>,
    val primaryNeed: PetNeed?,
    val suggestedBehaviour: PetBehaviour
)

class PetNeedsEngine(
    private val tuning: NeedTuning = NeedTuning()
) {
    fun advance(pet: PetInstance, nowEpochMs: Long): PetInstance {
        if (nowEpochMs <= pet.lastNeedsUpdateEpochMs) return pet

        val elapsedHours = (nowEpochMs - pet.lastNeedsUpdateEpochMs) / 3_600_000.0
        return pet.copy(
            hunger = min(100.0, pet.hunger + elapsedHours * tuning.hungerGainPerHour),
            dirtiness = min(100.0, pet.dirtiness + elapsedHours * tuning.dirtGainPerHour),
            lastNeedsUpdateEpochMs = nowEpochMs
        )
    }

    fun snapshot(pet: PetInstance): PetNeedsSnapshot {
        val needs = buildSet {
            if (pet.hunger >= tuning.hungryThreshold) add(PetNeed.FOOD)
            if (pet.dirtiness >= tuning.dirtyThreshold) add(PetNeed.CLEANING)
        }

        val primary = when {
            PetNeed.FOOD in needs && PetNeed.CLEANING in needs ->
                if (pet.hunger >= pet.dirtiness) PetNeed.FOOD else PetNeed.CLEANING
            PetNeed.FOOD in needs -> PetNeed.FOOD
            PetNeed.CLEANING in needs -> PetNeed.CLEANING
            else -> null
        }

        val behaviour = when (primary) {
            PetNeed.FOOD -> PetBehaviour.HUNGRY
            PetNeed.CLEANING -> PetBehaviour.DIRTY
            null -> if (pet.behaviour == PetBehaviour.HUNGRY || pet.behaviour == PetBehaviour.DIRTY) {
                PetBehaviour.IDLE
            } else {
                pet.behaviour
            }
        }

        return PetNeedsSnapshot(
            needs = needs,
            primaryNeed = primary,
            suggestedBehaviour = behaviour
        )
    }

    fun feed(pet: PetInstance, nowEpochMs: Long): PetInstance =
        feed(pet, nowEpochMs, tuning.feedReduction)

    fun feed(pet: PetInstance, nowEpochMs: Long, hungerReduction: Double): PetInstance {
        require(hungerReduction > 0.0 && hungerReduction <= 100.0)
        val advanced = advance(pet, nowEpochMs)
        return advanced.copy(
            hunger = (advanced.hunger - hungerReduction).coerceAtLeast(0.0),
            behaviour = PetBehaviour.HAPPY
        )
    }

    fun clean(pet: PetInstance, nowEpochMs: Long): PetInstance {
        val advanced = advance(pet, nowEpochMs)
        return advanced.copy(
            dirtiness = (advanced.dirtiness - tuning.cleanReduction).coerceAtLeast(0.0),
            behaviour = PetBehaviour.HAPPY
        )
    }
}
