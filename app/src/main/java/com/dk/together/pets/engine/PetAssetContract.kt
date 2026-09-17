package com.dk.together.pets.engine

/**
 * Stable logical asset keys. Actual Android drawable/resource IDs are deliberately not wired yet.
 * This lets the engine and future UI agree on what image/animation is required for each state.
 */
enum class PetVisualState {
    IDLE_FRONT,
    BLINK,
    WALK_LEFT,
    WALK_RIGHT,
    HAPPY,
    HUNGRY,
    DIRTY,
    SLEEPY
}

enum class ThoughtBubbleAsset {
    FOOD,
    BATH
}

enum class HatchVisualState {
    STAGE_01_PRISTINE,
    STAGE_02_WOBBLE,
    STAGE_03_SMALL_CRACK,
    STAGE_04_MEDIUM_CRACK,
    STAGE_05_HEAVY_CRACK,
    STAGE_06_SPLIT_SHELL,
    STAGE_07_HATCH_FLASH,
    STAGE_08_OPEN_SHELL
}

data class PetAssetKey(
    val species: PetSpecies,
    val visualState: PetVisualState
) {
    val canonicalName: String = "${species.name.lowercase()}_${visualState.name.lowercase()}"
}

object PetAssetMapper {
    fun visualStateFor(pet: PetInstance): PetVisualState = when (pet.behaviour) {
        PetBehaviour.HUNGRY -> PetVisualState.HUNGRY
        PetBehaviour.DIRTY -> PetVisualState.DIRTY
        PetBehaviour.HAPPY -> PetVisualState.HAPPY
        PetBehaviour.SLEEPING -> PetVisualState.SLEEPY
        PetBehaviour.WALKING -> if (pet.facing == FacingDirection.LEFT) {
            PetVisualState.WALK_LEFT
        } else {
            PetVisualState.WALK_RIGHT
        }
        PetBehaviour.IDLE -> PetVisualState.IDLE_FRONT
    }

    fun thoughtBubbleFor(needs: PetNeedsSnapshot): ThoughtBubbleAsset? = when (needs.primaryNeed) {
        PetNeed.FOOD -> ThoughtBubbleAsset.FOOD
        PetNeed.CLEANING -> ThoughtBubbleAsset.BATH
        null -> null
    }

    fun hatchVisualFor(stage: HatchStage): HatchVisualState = when (stage) {
        HatchStage.PRISTINE -> HatchVisualState.STAGE_01_PRISTINE
        HatchStage.WOBBLE -> HatchVisualState.STAGE_02_WOBBLE
        HatchStage.SMALL_CRACK -> HatchVisualState.STAGE_03_SMALL_CRACK
        HatchStage.MEDIUM_CRACK -> HatchVisualState.STAGE_04_MEDIUM_CRACK
        HatchStage.HEAVY_CRACK -> HatchVisualState.STAGE_05_HEAVY_CRACK
        HatchStage.SPLIT_SHELL -> HatchVisualState.STAGE_06_SPLIT_SHELL
        HatchStage.HATCH_FLASH -> HatchVisualState.STAGE_07_HATCH_FLASH
        HatchStage.READY_TO_CLAIM, HatchStage.CLAIMED -> HatchVisualState.STAGE_08_OPEN_SHELL
    }
}
