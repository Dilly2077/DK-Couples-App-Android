package com.dk.together.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.pets.engine.AndroidPetEngineRepository
import com.dk.together.pets.engine.NormalizedPosition
import com.dk.together.pets.engine.PetEngine
import com.dk.together.pets.engine.PetInstance
import com.dk.together.pets.engine.PetRoom
import com.dk.together.pets.engine.PetSpecies
import com.dk.together.pets.interaction.PetInteractionAction
import com.dk.together.pets.interaction.PetInteractionTarget
import com.dk.together.pets.play.AndroidPlayRepository
import com.dk.together.pets.play.PlayCompleteDecision
import com.dk.together.pets.play.PlayEngine
import com.dk.together.pets.play.PlayGesture
import com.dk.together.pets.play.PlayProgressTracker
import com.dk.together.pets.play.PlayStartDecision
import com.dk.together.pets.play.PlayToy
import com.dk.together.rewards.AndroidRewardRepository
import com.dk.together.rewards.RewardEngine
import com.dk.together.rewards.RewardHooks
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRoseDeep
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.abs

private enum class PlayroomPhase { ROOM, ACTIVITY, COMPLETE }

/** Persistent Playroom route: drag pet to toy -> finish mini-game -> reward once. */
@Composable
fun PersistentPlayroomScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val petRepository = remember { AndroidPetEngineRepository(context) }
    val petEngine = remember { PetEngine(petRepository) }
    val playRepository = remember { AndroidPlayRepository(context) }
    val rewardHooks = remember { RewardHooks(RewardEngine(AndroidRewardRepository(context))) }
    val playEngine = remember { PlayEngine(petEngine, playRepository, rewardHooks) }

    var pet by remember { mutableStateOf(petEngine.pets(System.currentTimeMillis()).firstOrNull()) }
    var phase by remember { mutableStateOf(PlayroomPhase.ROOM) }
    var activeToy by remember { mutableStateOf<PlayToy?>(null) }
    var interactionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var message by remember { mutableStateOf("Press and hold your pet, then place them by a toy.") }

    LaunchedEffect(pet?.id) {
        pet?.let { current ->
            pet = petEngine.moveToRoom(
                id = current.id,
                room = PetRoom.PLAYROOM,
                position = if (current.room == PetRoom.PLAYROOM) current.position else NormalizedPosition(0.50, 0.48),
                nowEpochMs = System.currentTimeMillis(),
            )
        }
    }

    if (pet == null) {
        EmptyPlayroomScreen(onBack, modifier)
        return
    }

    when (phase) {
        PlayroomPhase.ROOM -> PlayroomRoomScreen(
            pet = requireNotNull(pet),
            message = message,
            onBack = onBack,
            onPositionChanged = { next -> pet = pet?.copy(position = next) },
            onToyDrop = { toy, snap ->
                val id = UUID.randomUUID().toString()
                val moved = petEngine.moveToRoom(
                    id = requireNotNull(pet).id,
                    room = PetRoom.PLAYROOM,
                    position = snap,
                    nowEpochMs = System.currentTimeMillis(),
                )
                pet = moved
                val start = playEngine.start(moved.id, toy, id)
                when (val decision = start.decision) {
                    PlayStartDecision.Started,
                    PlayStartDecision.Resumed -> {
                        activeToy = toy
                        interactionId = id
                        phase = PlayroomPhase.ACTIVITY
                        message = playInstruction(toy)
                    }
                    PlayStartDecision.AlreadyCompleted -> message = "That play session already counted."
                    PlayStartDecision.PetNotFound -> message = "That pet could not be found."
                    PlayStartDecision.PetNotInPlayroom -> message = "Bring them back to the Playroom first."
                    is PlayStartDecision.CoolingDown -> {
                        val mins = ceil(decision.remainingMs / 60_000.0).toInt().coerceAtLeast(1)
                        message = "They've had a good play already. Try again in about $mins min."
                    }
                }
            },
            modifier = modifier,
        )

        PlayroomPhase.ACTIVITY -> PlayActivityScreen(
            pet = requireNotNull(pet),
            toy = requireNotNull(activeToy),
            message = message,
            onCancel = {
                phase = PlayroomPhase.ROOM
                message = "Play paused. Drop them by a toy whenever you're ready."
            },
            onFinished = {
                val result = playEngine.complete(
                    petId = requireNotNull(pet).id,
                    interactionId = interactionId,
                    activityComplete = true,
                    nowEpochMs = System.currentTimeMillis(),
                )
                result.pet?.let { pet = it }
                message = when (result.decision) {
                    PlayCompleteDecision.Completed -> "That was fun ♥ XP and Pet Coins earned."
                    PlayCompleteDecision.AlreadyCompleted -> "That play session was already counted."
                    PlayCompleteDecision.InteractionNotStarted -> "That play session expired."
                    PlayCompleteDecision.PetNotFound -> "That pet could not be found."
                    PlayCompleteDecision.PetNotInPlayroom -> "Bring them back to the Playroom first."
                    PlayCompleteDecision.ActivityIncomplete -> "A little more play first."
                }
                phase = PlayroomPhase.COMPLETE
            },
            modifier = modifier,
        )

        PlayroomPhase.COMPLETE -> PlayCompleteScreen(
            pet = requireNotNull(pet),
            toy = requireNotNull(activeToy),
            message = message,
            onBackToPlayroom = {
                phase = PlayroomPhase.ROOM
                activeToy = null
                message = "Press and hold to move them around the Playroom again."
            },
            onExit = onBack,
            modifier = modifier,
        )
    }
}

@Composable
private fun PlayroomRoomScreen(
    pet: PetInstance,
    message: String,
    onBack: () -> Unit,
    onPositionChanged: (NormalizedPosition) -> Unit,
    onToyDrop: (PlayToy, NormalizedPosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        Column(Modifier.fillMaxSize()) {
            PlayHeader("Playroom", message, onBack)
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                PlayroomBackdrop(Modifier.fillMaxSize())
                PetDragDropLayer(
                    petId = pet.id,
                    room = PetRoom.PLAYROOM,
                    initialPosition = pet.position,
                    modifier = Modifier.fillMaxSize(),
                    onPositionChanged = onPositionChanged,
                    onTransition = { transition ->
                        if (transition.action == PetInteractionAction.PLAY) {
                            PlayToy.fromTarget(transition.target)?.let { toy ->
                                onPositionChanged(transition.snapPosition)
                                onToyDrop(toy, transition.snapPosition)
                            }
                        }
                    },
                    targetContent = { target, highlighted -> PlayToyTarget(target, highlighted) },
                ) { carried ->
                    PlayPetAvatar(pet.species, carried)
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                color = EveluneCard,
                shape = RoundedCornerShape(22.dp),
            ) {
                Text(
                    "Choose a toy by carrying your pet to it. Rewards only happen after the activity is completed.",
                    color = EveluneMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayToyTarget(target: PetInteractionTarget, highlighted: Boolean) {
    val toy = PlayToy.fromTarget(target.id) ?: return
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (highlighted) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                color = Color(0xFFFFEEF7).copy(alpha = 0.72f),
                shape = RoundedCornerShape(24.dp),
            ) {}
        }
        PetToyArtwork(toy, Modifier.size(if (highlighted) 58.dp else 48.dp))
    }
}

@Composable
private fun PlayActivityScreen(
    pet: PetInstance,
    toy: PlayToy,
    message: String,
    onCancel: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracker = remember(pet.id, toy) { PlayProgressTracker(toy) }
    var completedCount by remember(pet.id, toy) { mutableIntStateOf(0) }
    var finished by remember(pet.id, toy) { mutableStateOf(false) }

    fun register(gesture: PlayGesture) {
        tracker.register(gesture)
        completedCount = tracker.completedCount
        if (tracker.complete && !finished) {
            finished = true
            onFinished()
        }
    }

    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        Column(Modifier.fillMaxSize()) {
            PlayHeader(toy.displayName, message, onCancel)
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                color = Color(0xFFFFFBFE),
                shape = RoundedCornerShape(30.dp),
                shadowElevation = 3.dp,
            ) {
                Column(
                    Modifier.fillMaxSize().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StorybookPetAvatar(pet.species, PetVisualState.PLAYING, Modifier.size(135.dp))
                    PetToyArtwork(toy, Modifier.size(86.dp))
                    Text(playInstruction(toy), color = EveluneInk, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 23.sp)
                    PlayHearts(completedCount, tracker.requiredCount)
                    PlayGestureSurface(toy = toy, onGesture = ::register, modifier = Modifier.fillMaxWidth().weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PlayGestureSurface(
    toy: PlayToy,
    onGesture: (PlayGesture) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (toy) {
        PlayToy.BALL,
        PlayToy.PLUSH -> Surface(
            onClick = { onGesture(PlayGesture.TAP) },
            modifier = modifier,
            color = Color(0xFFF5E9F8),
            shape = RoundedCornerShape(26.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(if (toy == PlayToy.BALL) "Tap to bounce" else "Tap to cuddle", color = EveluneRoseDeep, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            }
        }

        PlayToy.ROPE,
        PlayToy.FRISBEE -> {
            var dragStart by remember(toy) { mutableStateOf<Offset?>(null) }
            var dragEnd by remember(toy) { mutableStateOf<Offset?>(null) }
            Box(
                modifier = modifier
                    .background(Color(0xFFF5E9F8), RoundedCornerShape(26.dp))
                    .pointerInput(toy) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragStart = offset
                                dragEnd = offset
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                dragEnd = change.position
                            },
                            onDragCancel = {
                                dragStart = null
                                dragEnd = null
                            },
                            onDragEnd = {
                                val start = dragStart
                                val end = dragEnd
                                if (start != null && end != null) {
                                    val dx = end.x - start.x
                                    val dy = end.y - start.y
                                    if (toy == PlayToy.ROPE && abs(dx) >= 50f) {
                                        onGesture(if (dx < 0f) PlayGesture.SWIPE_LEFT else PlayGesture.SWIPE_RIGHT)
                                    } else if (toy == PlayToy.FRISBEE && dy <= -50f) {
                                        onGesture(PlayGesture.SWIPE_UP)
                                    }
                                }
                                dragStart = null
                                dragEnd = null
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (toy == PlayToy.ROPE) "Swipe left ↔ right" else "Flick the frisbee upward ↑",
                    color = EveluneRoseDeep,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun PlayHearts(completed: Int, required: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(required) { index ->
            Text(if (index < completed) "♥" else "♡", color = EveluneRoseDeep, fontSize = 23.sp)
        }
    }
}

@Composable
private fun PlayCompleteScreen(
    pet: PetInstance,
    toy: PlayToy,
    message: String,
    onBackToPlayroom: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(EveluneBackground), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            color = EveluneCard,
            shape = RoundedCornerShape(30.dp),
            shadowElevation = 3.dp,
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                StorybookPetAvatar(pet.species, PetVisualState.HAPPY, Modifier.size(150.dp))
                PetToyArtwork(toy, Modifier.size(70.dp))
                Text("${toy.displayName} time complete", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(message, color = EveluneMuted, fontSize = 13.sp)
                Surface(onClick = onBackToPlayroom, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = EveluneRoseDeep) {
                    Text("Back to Playroom", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 14.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                Surface(onClick = onExit, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color(0xFFF4E7F5)) {
                    Text("Back to Pet World", color = EveluneRoseDeep, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 13.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun PlayHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(onClick = onBack, shape = CircleShape, color = Color.White.copy(alpha = 0.82f)) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EveluneRoseDeep)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 31.sp)
            Text(subtitle, color = EveluneMuted, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun PlayroomBackdrop(modifier: Modifier = Modifier) {
    ApprovedEnvironmentArtwork(
        environment = PetEnvironmentArtwork.PLAYROOM,
        modifier = modifier,
    )
}

@Composable
private fun PlayPetAvatar(species: PetSpecies, carried: Boolean) {
    StorybookPetAvatar(
        species = species,
        state = if (carried) PetVisualState.CARRIED else PetVisualState.IDLE,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun EmptyPlayroomScreen(onBack: () -> Unit, modifier: Modifier) {
    Box(modifier.fillMaxSize().background(EveluneBackground), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PetCareArtwork("play", Modifier.size(64.dp))
            Text("Hatch a pet before opening the Playroom.", color = EveluneInk, fontWeight = FontWeight.SemiBold)
            Surface(onClick = onBack, shape = RoundedCornerShape(18.dp), color = EveluneRoseDeep) {
                Text("Back", color = Color.White, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
            }
        }
    }
}

private fun playInstruction(toy: PlayToy): String = when (toy) {
    PlayToy.BALL -> "Bounce the ball together"
    PlayToy.ROPE -> "Tug left and right"
    PlayToy.PLUSH -> "Give the plush some cuddles"
    PlayToy.FRISBEE -> "Flick the frisbee upward"
}
