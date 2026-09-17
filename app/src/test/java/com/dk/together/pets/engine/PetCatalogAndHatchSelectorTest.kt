package com.dk.together.pets.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PetCatalogAndHatchSelectorTest {
    @Test
    fun hatchWeightsTotalExactlyOneHundredPercent() {
        assertEquals(100.0, PetCatalog.totalWeightPercent, 0.0001)
        assertEquals(12, PetCatalog.all.size)
    }

    @Test
    fun firstThreeClaimedHatchesAreForcedUnique() {
        val selector = HatchSelector()
        val history = HatchHistory(
            totalClaimedHatches = 2,
            ownedSpecies = setOf(PetSpecies.PUPPY, PetSpecies.KITTEN)
        )

        val picked = selector.select(history, FixedRandom(0.0))

        assertFalse(picked.species in history.ownedSpecies)
    }

    @Test
    fun threeConsecutiveDuplicatesForceUnownedPet() {
        val selector = HatchSelector()
        val owned = setOf(PetSpecies.PUPPY, PetSpecies.KITTEN, PetSpecies.BUNNY)
        val history = HatchHistory(
            totalClaimedHatches = 8,
            ownedSpecies = owned,
            consecutiveDuplicateHatches = 3
        )

        val picked = selector.select(history, FixedRandom(0.0))

        assertFalse(picked.species in owned)
    }

    @Test
    fun legendaryPityLimitsPoolToTigerOrDragon() {
        val selector = HatchSelector()
        val history = HatchHistory(
            totalClaimedHatches = 40,
            hatchesSinceLegendaryOrBetter = 20
        )

        val picked = selector.select(history, FixedRandom(0.5))

        assertTrue(picked.species == PetSpecies.TIGER || picked.species == PetSpecies.DRAGON)
    }

    @Test
    fun dragonPityGuaranteesDragon() {
        val selector = HatchSelector()
        val history = HatchHistory(
            totalClaimedHatches = 150,
            hatchesSinceLegendaryOrBetter = 20,
            hatchesSinceDragon = 100
        )

        val picked = selector.select(history, FixedRandom(0.0))

        assertEquals(PetSpecies.DRAGON, picked.species)
    }

    @Test
    fun claimHistoryResetsRelevantPityCounters() {
        val selector = HatchSelector()
        val previous = HatchHistory(
            totalClaimedHatches = 25,
            ownedSpecies = setOf(PetSpecies.TIGER),
            consecutiveDuplicateHatches = 2,
            hatchesSinceLegendaryOrBetter = 19,
            hatchesSinceDragon = 40
        )

        val afterTiger = selector.historyAfterClaim(previous, PetSpecies.TIGER)
        assertEquals(0, afterTiger.hatchesSinceLegendaryOrBetter)
        assertEquals(41, afterTiger.hatchesSinceDragon)
        assertEquals(3, afterTiger.consecutiveDuplicateHatches)

        val afterDragon = selector.historyAfterClaim(afterTiger, PetSpecies.DRAGON)
        assertEquals(0, afterDragon.hatchesSinceLegendaryOrBetter)
        assertEquals(0, afterDragon.hatchesSinceDragon)
    }

    private class FixedRandom(private val value: Double) : RandomSource {
        override fun nextUnitDouble(): Double = value
    }
}
