package com.dk.together.pets.interaction

import com.dk.together.pets.engine.InteractionDropTarget
import com.dk.together.pets.engine.NormalizedPosition
import com.dk.together.pets.engine.PetRoom

class PetInteractionEngine(
    private val targets: List<PetInteractionTarget> = DefaultPetInteractionTargets.all,
) {
    fun beginDrag(
        petId: String,
        room: PetRoom,
        position: NormalizedPosition,
    ): PetInteractionSession = PetInteractionSession(
        petId = petId,
        room = room,
        position = position,
        phase = PetInteractionPhase.CARRIED,
    )

    fun dragTo(
        session: PetInteractionSession,
        position: NormalizedPosition,
    ): PetInteractionSession {
        require(session.phase == PetInteractionPhase.CARRIED || session.phase == PetInteractionPhase.HOVERING_TARGET) {
            "Pet must be carried before it can be dragged"
        }

        val hovered = targetsFor(session.room).firstOrNull { it.bounds.contains(position) }
        return session.copy(
            position = position,
            phase = if (hovered == null) PetInteractionPhase.CARRIED else PetInteractionPhase.HOVERING_TARGET,
            hoveredTarget = hovered?.id,
        )
    }

    fun release(session: PetInteractionSession): PetDropResult {
        require(session.phase == PetInteractionPhase.CARRIED || session.phase == PetInteractionPhase.HOVERING_TARGET) {
            "Pet must be carried before it can be released"
        }

        val target = session.hoveredTarget?.let { id -> targetsFor(session.room).firstOrNull { it.id == id } }
        if (target == null) {
            return PetDropResult(
                session = session.copy(
                    phase = PetInteractionPhase.IDLE,
                    hoveredTarget = null,
                ),
                transition = null,
            )
        }

        val transition = PetInteractionTransition(
            petId = session.petId,
            target = target.id,
            action = target.action,
            room = target.room,
            snapPosition = target.snapPosition,
        )
        return PetDropResult(
            session = session.copy(
                position = target.snapPosition,
                phase = PetInteractionPhase.TRANSITION_PENDING,
                hoveredTarget = target.id,
            ),
            transition = transition,
        )
    }

    fun cancel(session: PetInteractionSession): PetInteractionSession = session.copy(
        phase = PetInteractionPhase.IDLE,
        hoveredTarget = null,
    )

    fun finishTransition(
        session: PetInteractionSession,
        finalPosition: NormalizedPosition = session.position,
    ): PetInteractionSession {
        require(session.phase == PetInteractionPhase.TRANSITION_PENDING) {
            "No interaction transition is pending"
        }
        return session.copy(
            position = finalPosition,
            phase = PetInteractionPhase.IDLE,
            hoveredTarget = null,
        )
    }

    fun targetsFor(room: PetRoom): List<PetInteractionTarget> = targets.filter { it.room == room }
}

object DefaultPetInteractionTargets {
    val all: List<PetInteractionTarget> = listOf(
        PetInteractionTarget(
            id = InteractionDropTarget.KITCHEN_CHAIR_1,
            room = PetRoom.KITCHEN,
            action = PetInteractionAction.FEED,
            bounds = NormalizedRect(0.12, 0.52, 0.38, 0.90),
            snapPosition = NormalizedPosition(0.25, 0.72),
        ),
        PetInteractionTarget(
            id = InteractionDropTarget.KITCHEN_CHAIR_2,
            room = PetRoom.KITCHEN,
            action = PetInteractionAction.FEED,
            bounds = NormalizedRect(0.62, 0.52, 0.88, 0.90),
            snapPosition = NormalizedPosition(0.75, 0.72),
        ),
        PetInteractionTarget(
            id = InteractionDropTarget.GARDEN_HOT_TUB,
            room = PetRoom.GARDEN,
            action = PetInteractionAction.CLEAN,
            bounds = NormalizedRect(0.58, 0.32, 0.97, 0.78),
            snapPosition = NormalizedPosition(0.77, 0.56),
        ),
        PetInteractionTarget(
            id = InteractionDropTarget.PLAY_BALL,
            room = PetRoom.PLAYROOM,
            action = PetInteractionAction.PLAY,
            bounds = NormalizedRect(0.08, 0.55, 0.30, 0.91),
            snapPosition = NormalizedPosition(0.19, 0.72),
        ),
        PetInteractionTarget(
            id = InteractionDropTarget.PLAY_ROPE,
            room = PetRoom.PLAYROOM,
            action = PetInteractionAction.PLAY,
            bounds = NormalizedRect(0.31, 0.55, 0.50, 0.91),
            snapPosition = NormalizedPosition(0.405, 0.72),
        ),
        PetInteractionTarget(
            id = InteractionDropTarget.PLAY_PLUSH,
            room = PetRoom.PLAYROOM,
            action = PetInteractionAction.PLAY,
            bounds = NormalizedRect(0.51, 0.55, 0.70, 0.91),
            snapPosition = NormalizedPosition(0.605, 0.72),
        ),
        PetInteractionTarget(
            id = InteractionDropTarget.PLAY_FRISBEE,
            room = PetRoom.PLAYROOM,
            action = PetInteractionAction.PLAY,
            bounds = NormalizedRect(0.71, 0.55, 0.92, 0.91),
            snapPosition = NormalizedPosition(0.815, 0.72),
        ),
        PetInteractionTarget(
            id = InteractionDropTarget.PLAY_BALL,
            room = PetRoom.GARDEN,
            action = PetInteractionAction.PLAY,
            bounds = NormalizedRect(0.05, 0.58, 0.28, 0.92),
            snapPosition = NormalizedPosition(0.165, 0.74),
        ),
    )
}
