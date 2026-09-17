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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.pets.cleaning.CleanDecision
import com.dk.together.pets.cleaning.CleaningCoverageTracker
import com.dk.together.pets.cleaning.CleaningEngine
import com.dk.together.pets.engine.AndroidPetEngineRepository
import com.dk.together.pets.engine.NormalizedPosition
import com.dk.together.pets.engine.PetEngine
import com.dk.together.pets.engine.PetInstance
import com.dk.together.pets.engine.PetRoom
import com.dk.together.pets.engine.PetSpecies
import com.dk.together.pets.interaction.PetInteractionAction
import com.dk.together.rewards.AndroidRewardRepository
import com.dk.together.rewards.RewardEngine
import com.dk.together.rewards.RewardHooks
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRose
import com.dk.together.ui.theme.EveluneRoseDeep
import java.util.UUID

private enum class GardenCleaningPhase { GARDEN, SCRUBBING, COMPLETE }

/** Persistent Garden route: drag into hot tub -> scrub close-up -> clean/reward exactly once. */
@Composable
fun PersistentGardenCleaningScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val petRepository = remember { AndroidPetEngineRepository(context) }
    val petEngine = remember { PetEngine(petRepository) }
    val rewardHooks = remember { RewardHooks(RewardEngine(AndroidRewardRepository(context))) }
    val cleaningEngine = remember { CleaningEngine(petEngine, rewardHooks) }

    var pet by remember { mutableStateOf(petEngine.pets(System.currentTimeMillis()).firstOrNull()) }
    var phase by remember { mutableStateOf(GardenCleaningPhase.GARDEN) }
    var message by remember { mutableStateOf("Press and hold your pet, then place them in the hot tub.") }
    var interactionId by remember { mutableStateOf(UUID.randomUUID().toString()) }

    LaunchedEffect(pet?.id) {
        pet?.let { current ->
            val moved = petEngine.moveToRoom(
                id = current.id,
                room = PetRoom.GARDEN,
                position = if (current.room == PetRoom.GARDEN) current.position else NormalizedPosition(0.38, 0.74),
                nowEpochMs = System.currentTimeMillis(),
            )
            pet = moved
        }
    }

    if (pet == null) {
        EmptyGardenScreen(onBack = onBack, modifier = modifier)
        return
    }

    when (phase) {
        GardenCleaningPhase.GARDEN -> GardenHotTubScreen(
            pet = requireNotNull(pet),
            message = message,
            onBack = onBack,
            onPositionChanged = { next -> pet = pet?.copy(position = next) },
            onHotTub = { snap ->
                pet = petEngine.moveToRoom(
                    id = requireNotNull(pet).id,
                    room = PetRoom.GARDEN,
                    position = snap,
                    nowEpochMs = System.currentTimeMillis(),
                )
                interactionId = UUID.randomUUID().toString()
                phase = GardenCleaningPhase.SCRUBBING
                message = "Use the sponge until the visible dirt is gone."
            },
            modifier = modifier,
        )

        GardenCleaningPhase.SCRUBBING -> CleaningCloseUpScreen(
            pet = requireNotNull(pet),
            message = message,
            onBack = {
                phase = GardenCleaningPhase.GARDEN
                message = "Cleaning paused. Place them in the hot tub when you're ready."
            },
            onCompletion = { coverage ->
                val result = cleaningEngine.clean(
                    petId = requireNotNull(pet).id,
                    interactionId = interactionId,
                    coverage = coverage,
                    nowEpochMs = System.currentTimeMillis(),
                )
                result.pet?.let { pet = it }
                message = when (result.decision) {
                    CleanDecision.Completed -> "Fresh and happy ♥ XP and Pet Coins earned."
                    CleanDecision.AlreadyCompleted -> "All clean. That bath was already counted."
                    CleanDecision.PetNotDirtyEnough -> "They already look clean enough."
                    CleanDecision.PetNotFound -> "That pet could not be found."
                    CleanDecision.PetNotInGarden -> "Bring them back to the Garden first."
                    CleanDecision.CoverageIncomplete -> "A few dirty spots are still hiding."
                }
                phase = GardenCleaningPhase.COMPLETE
            },
            modifier = modifier,
        )

        GardenCleaningPhase.COMPLETE -> GardenCleaningCompleteScreen(
            pet = requireNotNull(pet),
            message = message,
            onBackToGarden = {
                phase = GardenCleaningPhase.GARDEN
                message = "Press and hold to move them around the Garden again."
            },
            onExit = onBack,
            modifier = modifier,
        )
    }
}

@Composable
private fun GardenHotTubScreen(
    pet: PetInstance,
    message: String,
    onBack: () -> Unit,
    onPositionChanged: (NormalizedPosition) -> Unit,
    onHotTub: (NormalizedPosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        Column(Modifier.fillMaxSize()) {
            GardenHeader("Evelune Garden", message, onBack)
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                GardenScene(Modifier.fillMaxSize())
                PetDragDropLayer(
                    petId = pet.id,
                    room = PetRoom.GARDEN,
                    initialPosition = pet.position,
                    modifier = Modifier.fillMaxSize(),
                    onPositionChanged = onPositionChanged,
                    onTransition = { transition ->
                        if (transition.action == PetInteractionAction.CLEAN) {
                            onPositionChanged(transition.snapPosition)
                            onHotTub(transition.snapPosition)
                        }
                    },
                ) { carried ->
                    GardenPetAvatar(pet.species, carried = carried)
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                color = EveluneCard,
                shape = RoundedCornerShape(22.dp),
            ) {
                Row(
                    Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PetCareArtwork("clean", Modifier.size(44.dp))
                    Column {
                        Text("Hot-tub cleaning", color = EveluneInk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("Drop your pet into the tub to start. No care meters are shown.", color = EveluneMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CleaningCloseUpScreen(
    pet: PetInstance,
    message: String,
    onBack: () -> Unit,
    onCompletion: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracker = remember(pet.id) { CleaningCoverageTracker() }
    var coverage by remember(pet.id) { mutableDoubleStateOf(0.0) }
    var cleanedCells by remember(pet.id) { mutableStateOf<Set<Int>>(emptySet()) }
    var completed by remember(pet.id) { mutableStateOf(false) }

    LaunchedEffect(coverage >= 0.85, completed) {
        if (coverage >= 0.85 && !completed) {
            completed = true
            onCompletion(coverage)
        }
    }

    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        Column(Modifier.fillMaxSize()) {
            GardenHeader("Bath time", message, onBack)
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp, vertical = 10.dp),
                color = Color(0xFFFFFBFD),
                shape = RoundedCornerShape(30.dp),
                shadowElevation = 3.dp,
            ) {
                Column(
                    Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Gently scrub the visible dirt", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text("Move the sponge across their coat. The dirt disappears where you clean.", color = EveluneMuted, fontSize = 12.sp)
                    ScrubPetSurface(
                        species = pet.species,
                        tracker = tracker,
                        cleanedCells = cleanedCells,
                        onScrubbed = {
                            coverage = tracker.coverage
                            cleanedCells = tracker.cleanedCells
                        },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PetCareArtwork("clean", Modifier.size(40.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                when {
                                    coverage < 0.35 -> "Lots of little spots left"
                                    coverage < 0.70 -> "Much better — keep going"
                                    coverage < 0.85 -> "Almost sparkling"
                                    else -> "Sparkling clean ♥"
                                },
                                color = EveluneInk,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            )
                            Text("The app judges visible scrub coverage, not a hidden stat bar.", color = EveluneMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrubPetSurface(
    species: PetSpecies,
    tracker: CleaningCoverageTracker,
    cleanedCells: Set<Int>,
    onScrubbed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var surfaceSize by remember { mutableStateOf(IntSize.Zero) }
    var sponge by remember { mutableStateOf<Offset?>(null) }

    fun scrub(offset: Offset) {
        if (surfaceSize.width <= 0 || surfaceSize.height <= 0) return
        val normalized = NormalizedPosition(
            x = (offset.x / surfaceSize.width.toFloat()).toDouble().coerceIn(0.0, 1.0),
            y = (offset.y / surfaceSize.height.toFloat()).toDouble().coerceIn(0.0, 1.0),
        )
        tracker.scrub(normalized)
        sponge = offset
        onScrubbed()
    }

    Box(
        modifier = modifier
            .background(Color(0xFFF5ECF7), RoundedCornerShape(28.dp))
            .onSizeChanged { surfaceSize = it }
            .pointerInput(species, surfaceSize) {
                detectDragGestures(
                    onDragStart = { offset -> scrub(offset) },
                    onDragEnd = { sponge = null },
                    onDragCancel = { sponge = null },
                    onDrag = { change, _ ->
                        change.consume()
                        scrub(change.position)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        ApprovedEnvironmentArtwork(PetEnvironmentArtwork.SPA, Modifier.fillMaxSize())
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.20f),
                cornerRadius = CornerRadius(28.dp.toPx()),
            )

            val cellW = size.width / tracker.columns
            val cellH = size.height / tracker.rows
            for (y in 0 until tracker.rows) {
                for (x in 0 until tracker.columns) {
                    val index = y * tracker.columns + x
                    if (index !in cleanedCells && (x * 7 + y * 11) % 4 == 0) {
                        drawCircle(
                            color = Color(0xFF8E756A).copy(alpha = 0.30f),
                            radius = (cellW.coerceAtMost(cellH) * 0.30f),
                            center = Offset((x + 0.5f) * cellW, (y + 0.5f) * cellH),
                        )
                    }
                }
            }

            sponge?.let { p ->
                drawCircle(Color.White.copy(alpha = 0.88f), radius = 24.dp.toPx(), center = p)
                drawCircle(Color(0xFFFFD8A8).copy(alpha = 0.95f), radius = 18.dp.toPx(), center = p)
            }
        }
        StorybookPetAvatar(
            species = species,
            state = PetVisualState.BATHING,
            modifier = Modifier.size(220.dp),
        )
    }
}

@Composable
private fun GardenCleaningCompleteScreen(
    pet: PetInstance,
    message: String,
    onBackToGarden: () -> Unit,
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
                Text("Fresh and comfy", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(message, color = EveluneMuted, fontSize = 13.sp)
                Surface(onClick = onBackToGarden, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = EveluneRoseDeep) {
                    Text("Back to Garden", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 14.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                Surface(onClick = onExit, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color(0xFFF4E7F5)) {
                    Text("Back to Pet World", color = EveluneRoseDeep, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 13.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun GardenHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(onClick = onBack, shape = CircleShape, color = Color.White.copy(alpha = 0.88f)) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EveluneRoseDeep)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontSize = 29.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = EveluneMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun GardenScene(modifier: Modifier = Modifier) {
    ApprovedEnvironmentArtwork(
        environment = PetEnvironmentArtwork.SPA,
        modifier = modifier,
    )
}

@Composable
private fun GardenPetAvatar(species: PetSpecies, carried: Boolean) {
    StorybookPetAvatar(
        species = species,
        state = if (carried) PetVisualState.CARRIED else PetVisualState.DIRTY,
        modifier = Modifier.size(if (carried) 94.dp else 88.dp),
    )
}

@Composable
private fun EmptyGardenScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(EveluneBackground), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PetCareArtwork("clean", Modifier.size(64.dp))
            Text("The Garden is ready", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Hatch a pet first, then bring them here for a bath.", color = EveluneMuted, fontSize = 14.sp)
            Surface(onClick = onBack, shape = RoundedCornerShape(20.dp), color = EveluneRoseDeep) {
                Text("Back to Pet World", color = Color.White, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
            }
        }
    }
}
