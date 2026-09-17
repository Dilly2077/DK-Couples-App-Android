package com.dk.together.pets.interaction

import com.dk.together.pets.engine.InteractionDropTarget
import com.dk.together.pets.engine.NormalizedPosition
import com.dk.together.pets.engine.PetRoom

enum class PetInteractionAction {
    FEED,
    CLEAN,
    PLAY,
}

enum class PetInteractionPhase {
    IDLE,
    CARRIED,
    HOVERING_TARGET,
    TRANSITION_PENDING,
}

data class NormalizedRect(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double,
) {
    init {
        require(left in 0.0..1.0)
        require(top in 0.0..1.0)
        require(right in 0.0..1.0)
        require(bottom in 0.0..1.0)
        require(right > left)
        require(bottom > top)
    }

    fun contains(position: NormalizedPosition): Boolean =
        position.x in left..right && position.y in top..bottom
}

data class PetInteractionTarget(
    val id: InteractionDropTarget,
    val room: PetRoom,
    val action: PetInteractionAction,
    val bounds: NormalizedRect,
    val snapPosition: NormalizedPosition,
)

data class PetInteractionSession(
    val petId: String,
    val room: PetRoom,
    val position: NormalizedPosition,
    val phase: PetInteractionPhase = PetInteractionPhase.IDLE,
    val hoveredTarget: InteractionDropTarget? = null,
)

data class PetInteractionTransition(
    val petId: String,
    val target: InteractionDropTarget,
    val action: PetInteractionAction,
    val room: PetRoom,
    val snapPosition: NormalizedPosition,
)

data class PetDropResult(
    val session: PetInteractionSession,
    val transition: PetInteractionTransition?,
)
