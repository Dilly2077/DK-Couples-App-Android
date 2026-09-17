package com.dk.together.pets.engine

import kotlin.random.Random

object PetCatalog {
    val all: List<PetDefinition> = listOf(
        PetDefinition(PetSpecies.PUPPY, "Puppy", PetRarity.COMMON, 12.5),
        PetDefinition(PetSpecies.KITTEN, "Kitten", PetRarity.COMMON, 12.5),
        PetDefinition(PetSpecies.BUNNY, "Bunny", PetRarity.COMMON, 12.5),
        PetDefinition(PetSpecies.DUCKLING, "Duckling", PetRarity.COMMON, 12.5),
        PetDefinition(PetSpecies.HEDGEHOG, "Hedgehog", PetRarity.UNCOMMON, 10.0),
        PetDefinition(PetSpecies.FERRET, "Ferret", PetRarity.UNCOMMON, 10.0),
        PetDefinition(PetSpecies.OTTER, "Otter", PetRarity.UNCOMMON, 10.0),
        PetDefinition(PetSpecies.FOX, "Fox", PetRarity.RARE, 5.0),
        PetDefinition(PetSpecies.RED_PANDA, "Red Panda", PetRarity.RARE, 5.0),
        PetDefinition(PetSpecies.AXOLOTL, "Axolotl", PetRarity.RARE, 5.0),
        PetDefinition(PetSpecies.TIGER, "Tiger", PetRarity.LEGENDARY, 4.0),
        PetDefinition(PetSpecies.DRAGON, "Dragon", PetRarity.MYTHIC, 1.0)
    )

    val bySpecies: Map<PetSpecies, PetDefinition> = all.associateBy { it.species }

    val totalWeightPercent: Double = all.sumOf { it.hatchWeightPercent }

    init {
        require(kotlin.math.abs(totalWeightPercent - 100.0) < 0.0001) {
            "Pet hatch weights must total 100%, found $totalWeightPercent%"
        }
        require(bySpecies.size == PetSpecies.entries.size) {
            "Every PetSpecies must have exactly one PetDefinition"
        }
    }
}

fun interface RandomSource {
    /** Returns a value in [0.0, 1.0). */
    fun nextUnitDouble(): Double
}

class KotlinRandomSource(
    private val random: Random = Random.Default
) : RandomSource {
    override fun nextUnitDouble(): Double = random.nextDouble()
}

class HatchSelector(
    private val definitions: List<PetDefinition> = PetCatalog.all,
    private val policy: HatchPolicy = HatchPolicy()
) {
    init {
        require(definitions.isNotEmpty()) { "definitions cannot be empty" }
        require(definitions.all { it.hatchWeightPercent > 0.0 }) { "all hatch weights must be positive" }
    }

    fun select(history: HatchHistory, random: RandomSource): PetDefinition {
        val candidates = when {
            history.hatchesSinceDragon >= policy.dragonPityAfter ->
                definitions.filter { it.species == PetSpecies.DRAGON }

            history.hatchesSinceLegendaryOrBetter >= policy.legendaryOrBetterPityAfter ->
                definitions.filter { it.rarity == PetRarity.LEGENDARY || it.rarity == PetRarity.MYTHIC }

            shouldForceUnique(history) -> {
                val unowned = definitions.filterNot { it.species in history.ownedSpecies }
                if (unowned.isNotEmpty()) unowned else definitions
            }

            else -> definitions
        }

        return weightedPick(candidates, random.nextUnitDouble())
    }

    private fun shouldForceUnique(history: HatchHistory): Boolean {
        val openingProtection = history.totalClaimedHatches < policy.guaranteedUniqueOpeningHatches
        val duplicateProtection = history.consecutiveDuplicateHatches >= policy.duplicateProtectionAfter
        return openingProtection || duplicateProtection
    }

    private fun weightedPick(candidates: List<PetDefinition>, unitRoll: Double): PetDefinition {
        require(candidates.isNotEmpty()) { "No hatch candidates available" }
        require(unitRoll >= 0.0 && unitRoll < 1.0) { "random roll must be in [0, 1)" }

        val total = candidates.sumOf { it.hatchWeightPercent }
        var cursor = unitRoll * total
        for (definition in candidates) {
            cursor -= definition.hatchWeightPercent
            if (cursor < 0.0) return definition
        }
        return candidates.last()
    }

    fun historyAfterClaim(previous: HatchHistory, species: PetSpecies): HatchHistory {
        val definition = PetCatalog.bySpecies.getValue(species)
        val wasDuplicate = species in previous.ownedSpecies
        val legendaryOrBetter = definition.rarity == PetRarity.LEGENDARY || definition.rarity == PetRarity.MYTHIC
        val isDragon = species == PetSpecies.DRAGON

        return previous.copy(
            totalClaimedHatches = previous.totalClaimedHatches + 1,
            ownedSpecies = previous.ownedSpecies + species,
            consecutiveDuplicateHatches = if (wasDuplicate) previous.consecutiveDuplicateHatches + 1 else 0,
            hatchesSinceLegendaryOrBetter = if (legendaryOrBetter) 0 else previous.hatchesSinceLegendaryOrBetter + 1,
            hatchesSinceDragon = if (isDragon) 0 else previous.hatchesSinceDragon + 1
        )
    }
}
