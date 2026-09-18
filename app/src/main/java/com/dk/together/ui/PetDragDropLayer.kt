package com.dk.together.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.random.Random

private data class PetRoamBounds(
    val minX: Double,
    val maxX: Double,
    val minY: Double,
    val maxY: Double,
)

private fun roamBounds(room: PetRoom): PetRoamBounds = when (room) {
    PetRoom.PLAYROOM -> PetRoamBounds(0.16, 0.84, 0.42, 0.78)
    PetRoom.KITCHEN -> PetRoamBounds(0.15, 0.85, 0.50, 0.80)
    PetRoom.GARDEN -> PetRoamBounds(0.12, 0.88, 0.50, 0.82)
    PetRoom.BATHROOM -> PetRoamBounds(0.18, 0.82, 0.50, 0.80)
}

/**
 * Reusable pet-room interaction layer.
 *
 * When the user is not touching the pet, it now wanders naturally around the safe floor area.
 * Roaming uses the recovered left/right walking artwork and pauses immediately when a long-press
 * carry begins. Drag/drop interactions still remain authoritative for feeding, cleaning and play.
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
    targetContent: @Composable (target: PetInteractionTarget, highlighted: Boolean) -> Unit = { _, highlighted ->
        DefaultDropTarget(highlighted)
    },
    petContent: @Composable (carried: Boolean, walking: Boolean, facingRight: Boolean) -> Unit,
) {
    val engine = remember(targets) { PetInteractionEngine(targets) }
    val latestOnPositionChanged by rememberUpdatedState(onPositionChanged)

    var session by remember(petId, room) {
        mutableStateOf(
            PetInteractionSession(
                petId = petId,
                room = room,
                position = initialPosition,
            )
        )
    }
    var roamingTarget by remember(petId, room) { mutableStateOf(initialPosition) }
    var roamingDurationMs by remember(petId, room) { mutableIntStateOf(2200) }
    var walking by remember(petId, room) { mutableStateOf(false) }
    var facingRight by remember(petId, room) { mutableStateOf(true) }
    var snapAfterCarry by remember(petId, room) { mutableStateOf(false) }

    val carried =
        session.phase == PetInteractionPhase.CARRIED ||
            session.phase == PetInteractionPhase.HOVERING_TARGET

    LaunchedEffect(petId, room, carried) {
        if (carried) {
            walking = false
            return@LaunchedEffect
        }

        val bounds = roamBounds(room)
        while (true) {
            delay(Random.nextLong(700L, 1900L))
            if (session.phase != PetInteractionPhase.IDLE) continue

            val start = session.position
            val target = NormalizedPosition(
                x = Random.nextDouble(bounds.minX, bounds.maxX),
                y = Random.nextDouble(bounds.minY, bounds.maxY),
            )
            val distance = hypot(target.x - start.x, target.y - start.y)
            if (distance < 0.08) continue

            facingRight = target.x >= start.x
            roamingDurationMs = (1400 + distance * 3600).toInt().coerceIn(1500, 4100)
            snapAfterCarry = false
            walking = true
            roamingTarget = target

            delay(roamingDurationMs.toLong())
            if (session.phase == PetInteractionPhase.IDLE) {
                session = session.copy(position = target)
                latestOnPositionChanged(target)
            }
            walking = false
        }
    }

    LaunchedEffect(snapAfterCarry) {
        if (snapAfterCarry) {
            delay(32)
            snapAfterCarry = false
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val sceneWidth = maxWidth
        val sceneHeight = maxHeight
        val sceneWidthPx = with(density) { sceneWidth.toPx() }.coerceAtLeast(1f)
        val sceneHeightPx = with(density) { sceneHeight.toPx() }.coerceAtLeast(1f)

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

        val animationSpec = if (snapAfterCarry) {
            snap<Float>()
        } else {
            tween<Float>(durationMillis = roamingDurationMs, easing = LinearEasing)
        }
        val animatedX by animateFloatAsState(
            targetValue = roamingTarget.x.toFloat(),
            animationSpec = animationSpec,
            label = "petRoamX",
        )
        val animatedY by animateFloatAsState(
            targetValue = roamingTarget.y.toFloat(),
            animationSpec = animationSpec,
            label = "petRoamY",
        )
        val carriedScale by animateFloatAsState(
            targetValue = if (carried) 1.08f else 1f,
            animationSpec = tween(160),
            label = "petCarryScale",
        )

        val bobTransition = rememberInfiniteTransition(label = "petWalkBob")
        val bob by bobTransition.animateFloat(
            initialValue = -2.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(260, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "petWalkBobOffset",
        )

        val displayX = if (carried) session.position.x.toFloat() else animatedX
        val displayY = if (carried) session.position.y.toFloat() else animatedY
        val petX = sceneWidth * displayX - petSize / 2
        val petY = sceneHeight * displayY - petSize / 2

        Box(
            modifier = Modifier
                .offset(x = petX, y = petY)
                .size(petSize)
                .scale(carriedScale)
                .graphicsLayer {
                    translationY = if (walking && !carried) bob else 0f
                }
                .zIndex(if (carried) 5f else 3f)
                .pointerInput(petId, room, sceneWidthPx, sceneHeightPx) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            walking = false
                            val current = NormalizedPosition(
                                x = displayX.toDouble().coerceIn(0.0, 1.0),
                                y = displayY.toDouble().coerceIn(0.0, 1.0),
                            )
                            session = session.copy(position = current)
                            roamingTarget = current
                            session = engine.beginDrag(
                                petId = petId,
                                room = room,
                                position = current,
                            )
                        },
                        onDragCancel = {
                            session = engine.cancel(session)
                            roamingTarget = session.position
                            snapAfterCarry = true
                        },
                        onDragEnd = {
                            val result = engine.release(session)
                            session = result.session
                            roamingTarget = session.position
                            snapAfterCarry = true
                            latestOnPositionChanged(session.position)
                            result.transition?.let { transition ->
                                onTransition(transition)
                                session = engine.finishTransition(session)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val next = NormalizedPosition(
                                x = (session.position.x + dragAmount.x / sceneWidthPx).coerceIn(0.0, 1.0),
                                y = (session.position.y + dragAmount.y / sceneHeightPx).coerceIn(0.0, 1.0),
                            )
                            facingRight = dragAmount.x >= 0f
                            session = engine.dragTo(session, next)
                            roamingTarget = next
                            latestOnPositionChanged(next)
                        },
                    )
                },
        ) {
            petContent(carried, walking && !carried, facingRight)
        }
    }
}

@Composable
private fun DefaultDropTarget(highlighted: Boolean) {
    if (!highlighted) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 3.dp,
                color = EveluneRoseDeep.copy(alpha = 0.78f),
                shape = RoundedCornerShape(24.dp),
            )
    )
}
