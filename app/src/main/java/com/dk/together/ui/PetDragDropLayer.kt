package com.dk.together.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dk.together.pets.engine.NormalizedPosition
import com.dk.together.pets.engine.PetRoom
import com.dk.together.pets.interaction.DefaultPetInteractionTargets
import com.dk.together.pets.interaction.PetInteractionEngine
import com.dk.together.pets.interaction.PetInteractionPhase
import com.dk.together.pets.interaction.PetInteractionSession
import com.dk.together.pets.interaction.PetInteractionTarget
import com.dk.together.pets.interaction.PetInteractionTransition
import com.dk.together.ui.theme.EveluneRoseDeep

/**
 * Reusable Compose surface for the pet world's press/hold -> carry -> hover -> drop flow.
 *
 * It intentionally owns no feeding/cleaning/play logic. A successful drop emits a transition;
 * the destination mini-game decides whether the action actually completed and only then emits a
 * reward hook.
 */
@Composable
fun PetDragDropLayer(
    petId: String,
    room: PetRoom,
    initialPosition: NormalizedPosition,
    modifier: Modifier = Modifier,
    petSize: Dp = 104.dp,
    targets: List<PetInteractionTarget> = DefaultPetInteractionTargets.all.filter { it.room == room },
    onPositionChanged: (NormalizedPosition) -> Unit = {},
    onTransition: (PetInteractionTransition) -> Unit,
    targetContent: @Composable BoxWithConstraintsScope.(target: PetInteractionTarget, highlighted: Boolean) -> Unit = { target, highlighted ->
        DefaultDropTarget(target, highlighted)
    },
    petContent: @Composable (carried: Boolean) -> Unit,
) {
    val engine = remember(targets) { PetInteractionEngine(targets) }
    var session by remember(petId, room) {
        mutableStateOf(
            PetInteractionSession(
                petId = petId,
                room = room,
                position = initialPosition,
            )
        )
    }

    BoxWithConstraints(modifier = modifier) {
        val sceneWidth = maxWidth
        val sceneHeight = maxHeight

        targets.forEach { target ->
            val width = sceneWidth * (target.bounds.right - target.bounds.left).toFloat()
            val height = sceneHeight * (target.bounds.bottom - target.bounds.top).toFloat()
            val x = sceneWidth * target.bounds.left.toFloat()
            val y = sceneHeight * target.bounds.top.toFloat()
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(width = width, height = height)
                    .zIndex(1f),
            ) {
                targetContent(target, session.hoveredTarget == target.id)
            }
        }

        val carried = session.phase == PetInteractionPhase.CARRIED || session.phase == PetInteractionPhase.HOVERING_TARGET
        val carriedScale by animateFloatAsState(if (carried) 1.08f else 1f, label = "petCarryScale")
        val petX = sceneWidth * session.position.x.toFloat() - petSize / 2
        val petY = sceneHeight * session.position.y.toFloat() - petSize / 2

        Box(
            modifier = Modifier
                .offset(x = petX, y = petY)
                .size(petSize)
                .scale(carriedScale)
                .zIndex(if (carried) 5f else 3f)
                .pointerInput(petId, room, sceneWidth, sceneHeight) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            session = engine.beginDrag(
                                petId = petId,
                                room = room,
                                position = session.position,
                            )
                        },
                        onDragCancel = {
                            session = engine.cancel(session)
                        },
                        onDragEnd = {
                            val result = engine.release(session)
                            session = result.session
                            onPositionChanged(session.position)
                            result.transition?.let { transition ->
                                onTransition(transition)
                                session = engine.finishTransition(session)
                            }
                        },
                        onDrag = { _, dragAmount ->
                            val widthPx = sceneWidth.toPx().coerceAtLeast(1f)
                            val heightPx = sceneHeight.toPx().coerceAtLeast(1f)
                            val next = NormalizedPosition(
                                x = (session.position.x + dragAmount.x / widthPx).coerceIn(0.0, 1.0),
                                y = (session.position.y + dragAmount.y / heightPx).coerceIn(0.0, 1.0),
                            )
                            session = engine.dragTo(session, next)
                            onPositionChanged(next)
                        },
                    )
                },
        ) {
            petContent(carried)
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.DefaultDropTarget(
    target: PetInteractionTarget,
    highlighted: Boolean,
) {
    if (!highlighted) return
    Box(
        modifier = Modifier
            .size(maxWidth, maxHeight)
            .border(
                width = 3.dp,
                color = EveluneRoseDeep.copy(alpha = 0.78f),
                shape = RoundedCornerShape(24.dp),
            )
    )
}
