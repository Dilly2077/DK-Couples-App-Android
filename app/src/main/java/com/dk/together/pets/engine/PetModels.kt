package com.dk.together.pets.engine

enum class PetRarity {
    COMMON,
    UNCOMMON,
    RARE,
    LEGENDARY,
    MYTHIC
}

enum class PetSpecies {
    PUPPY,
    KITTEN,
    BUNNY,
    DUCKLING,
    HEDGEHOG,
    FERRET,
    OTTER,
    FOX,
    RED_PANDA,
    AXOLOTL,
    TIGER,
    DRAGON
}

enum class PetRoom {
    PLAYROOM,
    KITCHEN,
    BATHROOM,
    GARDEN
}

enum class PetBehaviour {
    IDLE,
    WALKING,
    HAPPY,
    HUNGRY,
    DIRTY,
    SLEEPING
}

enum class PetNeed {
    FOOD,
    CLEANING
}

enum class FacingDirection {
    LEFT,
    RIGHT
}

data class NormalizedPosition(
    val x: Double,
    val y: Double
) {
    init {
        require(x in 0.0..1.0) { "x must be between 0 and 1" }
        require(y in 0.0..1.0) { "y must be between 0 and 1" }
    }
}

data class PetDefinition(
    val species: PetSpecies,
    val displayName: String,
    val rarity: PetRarity,
    val hatchWeightPercent: Double
)

data class PetInstance(
    val id: String,
    val species: PetSpecies,
    val nickname: String? = null,
    val hatchedAtEpochMs: Long,
    val room: PetRoom = PetRoom.PLAYROOM,
    val position: NormalizedPosition = NormalizedPosition(0.5, 0.72),
    val facing: FacingDirection = FacingDirection.RIGHT,
    val behaviour: PetBehaviour = PetBehaviour.IDLE,
    /** Internal simulation value only. 0 = full, 100 = very hungry. */
    val hunger: Double = 0.0,
    /** Internal simulation value only. 0 = clean, 100 = very dirty. */
    val dirtiness: Double = 0.0,
    val lastNeedsUpdateEpochMs: Long = hatchedAtEpochMs
) {
    init {
        require(hunger in 0.0..100.0) { "hunger must be between 0 and 100" }
        require(dirtiness in 0.0..100.0) { "dirtiness must be between 0 and 100" }
    }
}

data class HatchHistory(
    val totalClaimedHatches: Int = 0,
    val ownedSpecies: Set<PetSpecies> = emptySet(),
    val consecutiveDuplicateHatches: Int = 0,
    val hatchesSinceLegendaryOrBetter: Int = 0,
    val hatchesSinceDragon: Int = 0
)

data class HatchPolicy(
    val guaranteedUniqueOpeningHatches: Int = 3,
    val duplicateProtectionAfter: Int = 3,
    val legendaryOrBetterPityAfter: Int = 20,
    val dragonPityAfter: Int = 100
)
