package com.dk.together.pets.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PetInteractionAssetContractTest {

    @Test
    fun `all asset canonical names are unique`() {
        val names = buildList {
            addAll(EnvironmentLayerAsset.entries.map { it.canonicalName })
            addAll(FeedingPropAsset.entries.map { it.canonicalName })
            addAll(CleaningPropAsset.entries.map { it.canonicalName })
            addAll(PlayPropAsset.entries.map { it.canonicalName })
            addAll(FoodAsset.entries.map { it.canonicalName })
            addAll(EconomyAsset.entries.map { it.canonicalName })
        }

        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun `every room has an ordered layer contract`() {
        PetRoom.entries.forEach { room ->
            val layers = RoomAssetContract.layersFor(room)
            assertTrue("$room must have at least one layer", layers.isNotEmpty())
            assertEquals("$room layer contract contains duplicates", layers.size, layers.distinct().size)
        }
    }

    @Test
    fun `care visual states have stable pet canonical names`() {
        val species = PetSpecies.PUPPY
        val expected = mapOf(
            PetVisualState.CARRIED to "puppy_carried",
            PetVisualState.SEATED to "puppy_seated",
            PetVisualState.EATING to "puppy_eating",
            PetVisualState.BATHING to "puppy_bathing",
            PetVisualState.PLAYING to "puppy_playing",
            PetVisualState.LEVEL_UP to "puppy_level_up"
        )

        expected.forEach { (state, canonical) ->
            assertEquals(canonical, PetAssetKey(species, state).canonicalName)
        }
    }
}
