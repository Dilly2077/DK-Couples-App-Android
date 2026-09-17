package com.dk.together.pets.engine

/**
 * Stable logical keys for the v0.2 pet-care art pack.
 * These are data contracts only: gameplay and Android drawable bindings are added later.
 */
enum class EnvironmentLayerAsset(val canonicalName: String) {
    PLAYROOM_BG("playroom_bg"),
    PLAYROOM_PROPS_BACK("playroom_props_back"),
    PLAYROOM_TOY_ZONE("playroom_toy_zone"),
    PLAYROOM_FOREGROUND("playroom_foreground"),

    KITCHEN_BG("kitchen_bg"),
    KITCHEN_BACK_FURNITURE("kitchen_back_furniture"),
    KITCHEN_CHAIR_1("kitchen_chair_1"),
    KITCHEN_CHAIR_2("kitchen_chair_2"),
    KITCHEN_TABLE_FRONT("kitchen_table_front"),
    KITCHEN_FOREGROUND("kitchen_foreground"),

    BATHROOM_BG("bathroom_bg"),
    BATHROOM_BACK_PROPS("bathroom_back_props"),
    BATHROOM_FOREGROUND("bathroom_foreground"),

    GARDEN_BG("garden_bg"),
    GARDEN_SPA_BACK("garden_spa_back"),
    HOT_TUB_BACK_RIM("hot_tub_back_rim"),
    HOT_TUB_WATER_OVERLAY("hot_tub_water_overlay"),
    HOT_TUB_BUBBLE_OVERLAY("hot_tub_bubble_overlay"),
    HOT_TUB_FRONT_RIM("hot_tub_front_rim"),
    GARDEN_FOREGROUND("garden_foreground")
}

enum class FeedingPropAsset(val canonicalName: String) {
    FOOD_BOWL("food_bowl"),
    PLATE("food_plate"),
    FEEDING_MAT("feeding_mat")
}

enum class CleaningPropAsset(val canonicalName: String) {
    SPONGE("cleaning_sponge"),
    SOAP_BOTTLE("cleaning_soap_bottle"),
    BUBBLES("cleaning_bubbles"),
    SPLASH("cleaning_splash"),
    CLEAN_SPARKLE("cleaning_sparkle"),
    DIRT_OVERLAY_01("dirt_overlay_01"),
    DIRT_OVERLAY_02("dirt_overlay_02")
}

enum class PlayPropAsset(val canonicalName: String) {
    TOY_BASKET("toy_basket"),
    BALL("toy_ball"),
    ROPE_TOY("toy_rope"),
    PLUSH_TOY("toy_plush"),
    FRISBEE("toy_frisbee")
}

enum class FoodAsset(val canonicalName: String) {
    KIBBLE("food_kibble"),
    FISH("food_fish"),
    CARROT("food_carrot"),
    BERRY_BOWL("food_berry_bowl"),
    APPLE_SLICES("food_apple_slices"),
    MILK_BOWL("food_milk_bowl"),
    BISCUIT("food_biscuit"),
    SALAD_BOWL("food_salad_bowl"),
    CUPCAKE_TREAT("food_cupcake_treat"),
    PREMIUM_FEAST_TRAY("food_premium_feast_tray")
}

enum class EconomyAsset(val canonicalName: String) {
    PET_COIN("pet_coin"),
    XP_BURST("xp_burst"),
    XP_FLOAT("xp_float"),
    COIN_FLOAT("pet_coin_float"),
    LEVEL_UP_BADGE("level_up_badge"),
    INVENTORY_SLOT("inventory_slot"),
    SHOP_TILE("shop_tile"),
    VALID_DROP_GLOW("valid_drop_glow")
}

enum class InteractionDropTarget {
    KITCHEN_CHAIR_1,
    KITCHEN_CHAIR_2,
    GARDEN_HOT_TUB,
    PLAY_BALL,
    PLAY_ROPE,
    PLAY_PLUSH,
    PLAY_FRISBEE
}

object RoomAssetContract {
    fun layersFor(room: PetRoom): List<EnvironmentLayerAsset> = when (room) {
        PetRoom.PLAYROOM -> listOf(
            EnvironmentLayerAsset.PLAYROOM_BG,
            EnvironmentLayerAsset.PLAYROOM_PROPS_BACK,
            EnvironmentLayerAsset.PLAYROOM_TOY_ZONE,
            EnvironmentLayerAsset.PLAYROOM_FOREGROUND
        )
        PetRoom.KITCHEN -> listOf(
            EnvironmentLayerAsset.KITCHEN_BG,
            EnvironmentLayerAsset.KITCHEN_BACK_FURNITURE,
            EnvironmentLayerAsset.KITCHEN_CHAIR_1,
            EnvironmentLayerAsset.KITCHEN_CHAIR_2,
            EnvironmentLayerAsset.KITCHEN_TABLE_FRONT,
            EnvironmentLayerAsset.KITCHEN_FOREGROUND
        )
        PetRoom.BATHROOM -> listOf(
            EnvironmentLayerAsset.BATHROOM_BG,
            EnvironmentLayerAsset.BATHROOM_BACK_PROPS,
            EnvironmentLayerAsset.BATHROOM_FOREGROUND
        )
        PetRoom.GARDEN -> listOf(
            EnvironmentLayerAsset.GARDEN_BG,
            EnvironmentLayerAsset.GARDEN_SPA_BACK,
            EnvironmentLayerAsset.HOT_TUB_BACK_RIM,
            EnvironmentLayerAsset.HOT_TUB_WATER_OVERLAY,
            EnvironmentLayerAsset.HOT_TUB_BUBBLE_OVERLAY,
            EnvironmentLayerAsset.HOT_TUB_FRONT_RIM,
            EnvironmentLayerAsset.GARDEN_FOREGROUND
        )
    }
}
