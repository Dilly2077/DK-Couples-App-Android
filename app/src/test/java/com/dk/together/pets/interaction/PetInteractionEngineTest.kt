package com.dk.together.pets.interaction

import com.dk.together.pets.engine.InteractionDropTarget
import com.dk.together.pets.engine.NormalizedPosition
import com.dk.together.pets.engine.PetRoom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PetInteractionEngineTest {

    @Test
    fun `press and hold enters carried state`() {
        val engine = PetInteractionEngine()
        val session = engine.beginDrag(
            petId = "pet-1",
            room = PetRoom.KITCHEN,
            position = NormalizedPosition(0.5, 0.7),
        )

        assertEquals(PetInteractionPhase.CARRIED, session.phase)
        assertNull(session.hoveredTarget)
    }

    @Test
    fun `dragging over a valid target enters hovering state`() {
        val engine = PetInteractionEngine()
        val carried = engine.beginDrag("pet-1", PetRoom.KITCHEN, NormalizedPosition(0.5, 0.7))
        val hovering = engine.dragTo(carried, NormalizedPosition(0.25, 0.72))

        assertEquals(PetInteractionPhase.HOVERING_TARGET, hovering.phase)
        assertEquals(InteractionDropTarget.KITCHEN_CHAIR_1, hovering.hoveredTarget)
    }

    @Test
    fun `dragging away from a target clears hover`() {
        val engine = PetInteractionEngine()
        val carried = engine.beginDrag("pet-1", PetRoom.KITCHEN, NormalizedPosition(0.5, 0.7))
        val hovering = engine.dragTo(carried, NormalizedPosition(0.25, 0.72))
        val movedAway = engine.dragTo(hovering, NormalizedPosition(0.50, 0.20))

        assertEquals(PetInteractionPhase.CARRIED, movedAway.phase)
        assertNull(movedAway.hoveredTarget)
    }

    @Test
    fun `valid kitchen drop snaps and emits feed transition`() {
        val engine = PetInteractionEngine()
        val carried = engine.beginDrag("pet-1", PetRoom.KITCHEN, NormalizedPosition(0.5, 0.7))
        val hovering = engine.dragTo(carried, NormalizedPosition(0.25, 0.72))
        val result = engine.release(hovering)

        assertEquals(PetInteractionPhase.TRANSITION_PENDING, result.session.phase)
        assertEquals(NormalizedPosition(0.25, 0.72), result.session.position)
        assertEquals(PetInteractionAction.FEED, result.transition?.action)
        assertEquals(InteractionDropTarget.KITCHEN_CHAIR_1, result.transition?.target)
    }

    @Test
    fun `invalid drop keeps free position and does not transition`() {
        val engine = PetInteractionEngine()
        val carried = engine.beginDrag("pet-1", PetRoom.KITCHEN, NormalizedPosition(0.5, 0.7))
        val free = engine.dragTo(carried, NormalizedPosition(0.50, 0.20))
        val result = engine.release(free)

        assertEquals(PetInteractionPhase.IDLE, result.session.phase)
        assertEquals(NormalizedPosition(0.50, 0.20), result.session.position)
        assertNull(result.transition)
    }

    @Test
    fun `garden hot tub emits clean transition`() {
        val engine = PetInteractionEngine()
        val carried = engine.beginDrag("pet-2", PetRoom.GARDEN, NormalizedPosition(0.4, 0.7))
        val hovering = engine.dragTo(carried, NormalizedPosition(0.78, 0.55))
        val result = engine.release(hovering)

        assertEquals(PetInteractionAction.CLEAN, result.transition?.action)
        assertEquals(InteractionDropTarget.GARDEN_HOT_TUB, result.transition?.target)
        assertEquals(NormalizedPosition(0.77, 0.56), result.transition?.snapPosition)
    }

    @Test
    fun `playroom toy emits play transition`() {
        val engine = PetInteractionEngine()
        val carried = engine.beginDrag("pet-3", PetRoom.PLAYROOM, NormalizedPosition(0.5, 0.7))
        val hovering = engine.dragTo(carried, NormalizedPosition(0.82, 0.72))
        val result = engine.release(hovering)

        assertEquals(PetInteractionAction.PLAY, result.transition?.action)
        assertEquals(InteractionDropTarget.PLAY_FRISBEE, result.transition?.target)
    }

    @Test
    fun `targets are room scoped`() {
        val engine = PetInteractionEngine()

        assertTrue(engine.targetsFor(PetRoom.KITCHEN).all { it.room == PetRoom.KITCHEN })
        assertTrue(engine.targetsFor(PetRoom.GARDEN).all { it.room == PetRoom.GARDEN })
        assertTrue(engine.targetsFor(PetRoom.PLAYROOM).all { it.room == PetRoom.PLAYROOM })
        assertTrue(engine.targetsFor(PetRoom.BATHROOM).isEmpty())
    }

    @Test
    fun `finishing transition returns to idle at snapped position`() {
        val engine = PetInteractionEngine()
        val carried = engine.beginDrag("pet-1", PetRoom.KITCHEN, NormalizedPosition(0.5, 0.7))
        val hovering = engine.dragTo(carried, NormalizedPosition(0.25, 0.72))
        val dropped = engine.release(hovering).session
        val finished = engine.finishTransition(dropped)

        assertEquals(PetInteractionPhase.IDLE, finished.phase)
        assertNull(finished.hoveredTarget)
        assertEquals(NormalizedPosition(0.25, 0.72), finished.position)
    }
}
