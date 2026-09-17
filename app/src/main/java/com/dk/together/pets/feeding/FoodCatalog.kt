package com.dk.together.pets.feeding

enum class FoodId {
    KIBBLE,
    FISH,
    CARROT,
    BERRY_BOWL,
    APPLE_SLICES,
    MILK_BOWL,
    BISCUIT,
    SALAD_BOWL,
    CUPCAKE_TREAT,
    PREMIUM_FEAST_TRAY,
}

data class FoodDefinition(
    val id: FoodId,
    val displayName: String,
    /** Amount removed from the pet's hidden hunger value. */
    val hungerReduction: Double,
    /** Canonical v0.3 asset key. */
    val assetKey: String,
) {
    init {
        require(hungerReduction > 0.0 && hungerReduction <= 100.0)
        require(assetKey.isNotBlank())
    }
}

object FoodCatalog {
    val all: List<FoodDefinition> = listOf(
        FoodDefinition(FoodId.KIBBLE, "Kibble", 35.0, "food_kibble"),
        FoodDefinition(FoodId.FISH, "Fish", 45.0, "food_fish"),
        FoodDefinition(FoodId.CARROT, "Carrot", 25.0, "food_carrot"),
        FoodDefinition(FoodId.BERRY_BOWL, "Berry bowl", 30.0, "food_berry_bowl"),
        FoodDefinition(FoodId.APPLE_SLICES, "Apple slices", 25.0, "food_apple_slices"),
        FoodDefinition(FoodId.MILK_BOWL, "Milk bowl", 30.0, "food_milk_bowl"),
        FoodDefinition(FoodId.BISCUIT, "Biscuit", 20.0, "food_biscuit"),
        FoodDefinition(FoodId.SALAD_BOWL, "Salad bowl", 35.0, "food_salad_bowl"),
        FoodDefinition(FoodId.CUPCAKE_TREAT, "Cupcake treat", 20.0, "food_cupcake_treat"),
        FoodDefinition(FoodId.PREMIUM_FEAST_TRAY, "Premium feast", 70.0, "food_premium_feast_tray"),
    )

    private val byId = all.associateBy { it.id }

    fun get(id: FoodId): FoodDefinition = requireNotNull(byId[id]) { "Unknown food: $id" }

    /**
     * Small local starter pack so feeding can be used before the Pet Coin shop chunk lands.
     * Shop purchases will add to the same inventory later.
     */
    val starterInventory: Map<FoodId, Int> = linkedMapOf(
        FoodId.KIBBLE to 3,
        FoodId.FISH to 1,
        FoodId.CARROT to 2,
        FoodId.BERRY_BOWL to 1,
        FoodId.BISCUIT to 2,
    )
}
