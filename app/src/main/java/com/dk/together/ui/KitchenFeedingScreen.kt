package com.dk.together.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.dk.together.pets.feeding.AndroidFeedingRepository
import com.dk.together.pets.feeding.FeedDecision
import com.dk.together.pets.feeding.FeedingEngine
import com.dk.together.pets.feeding.FoodCatalog
import com.dk.together.pets.feeding.FoodDefinition
import com.dk.together.pets.feeding.FoodId
import com.dk.together.pets.interaction.PetInteractionAction
import com.dk.together.rewards.AndroidRewardRepository
import com.dk.together.rewards.RewardEngine
import com.dk.together.rewards.RewardHooks
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRoseDeep
import java.util.UUID

private enum class KitchenPhase { ROAMING, SEATED, EATING, CELEBRATING }

/**
 * Persistent Kitchen feeding route. It uses the real local pet, feeding-inventory and reward stores.
 * Production v0.3 images can replace the temporary Compose-drawn kitchen/pet without changing flow.
 */
@Composable
fun PersistentKitchenFeedingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val petRepository = remember { AndroidPetEngineRepository(context) }
    val petEngine = remember { PetEngine(petRepository) }
    val feedingRepository = remember { AndroidFeedingRepository(context) }
    val rewardHooks = remember { RewardHooks(RewardEngine(AndroidRewardRepository(context))) }
    val feedingEngine = remember { FeedingEngine(petEngine, feedingRepository, rewardHooks) }

    var pet by remember { mutableStateOf(petEngine.pets(System.currentTimeMillis()).firstOrNull()) }
    var inventory by remember { mutableStateOf<Map<FoodId, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        feedingEngine.ensureStarterInventory()
        inventory = feedingEngine.inventory()
    }

    LaunchedEffect(pet?.id) {
        pet?.let { current ->
            val moved = petEngine.moveToRoom(
                id = current.id,
                room = PetRoom.KITCHEN,
                position = if (current.room == PetRoom.KITCHEN) current.position else NormalizedPosition(0.5, 0.78),
                nowEpochMs = System.currentTimeMillis(),
            )
            pet = moved
        }
    }

    if (pet == null) {
        EmptyKitchenScreen(onBack = onBack, modifier = modifier)
        return
    }

    KitchenFeedingScreen(
        pet = requireNotNull(pet),
        inventory = inventory,
        onBack = onBack,
        onPositionChanged = { next -> pet = pet?.copy(position = next) },
        onFeed = { food, interactionId ->
            val result = feedingEngine.feed(
                petId = requireNotNull(pet).id,
                foodId = food.id,
                interactionId = interactionId,
                nowEpochMs = System.currentTimeMillis(),
            )
            inventory = feedingEngine.inventory()
            result.pet?.let { pet = it }
            result.decision
        },
        modifier = modifier,
    )
}

@Composable
private fun KitchenFeedingScreen(
    pet: PetInstance,
    inventory: Map<FoodId, Int>,
    onBack: () -> Unit,
    onPositionChanged: (NormalizedPosition) -> Unit,
    onFeed: (FoodDefinition, String) -> FeedDecision,
    modifier: Modifier = Modifier,
) {
    var phase by remember(pet.id) { mutableStateOf(KitchenPhase.ROAMING) }
    var message by remember(pet.id) { mutableStateOf("Press and hold your pet, then place them on a chair.") }

    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(onClick = onBack, shape = CircleShape, color = Color.White.copy(alpha = .88f)) {
                    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EveluneRoseDeep)
                    }
                }
                Column {
                    Text("Evelune Kitchen", color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                    Text(message, color = EveluneMuted, fontSize = 12.sp)
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp),
            ) {
                KitchenScene(Modifier.fillMaxSize())

                if (phase == KitchenPhase.ROAMING) {
                    PetDragDropLayer(
                        petId = pet.id,
                        room = PetRoom.KITCHEN,
                        initialPosition = pet.position,
                        modifier = Modifier.fillMaxSize(),
                        onPositionChanged = onPositionChanged,
                        onTransition = { transition ->
                            if (transition.action == PetInteractionAction.FEED) {
                                onPositionChanged(transition.snapPosition)
                                phase = KitchenPhase.SEATED
                                message = "Seated and ready. Choose some food."
                            }
                        },
                    ) { carried ->
                        PetKitchenAvatar(pet.species, carried = carried, seated = false, eating = false)
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(Modifier.padding(bottom = 54.dp)) {
                            PetKitchenAvatar(
                                species = pet.species,
                                carried = false,
                                seated = true,
                                eating = phase == KitchenPhase.EATING,
                            )
                        }
                    }
                }
            }

            if (phase != KitchenPhase.ROAMING) {
                FoodInventoryTray(
                    inventory = inventory,
                    enabled = phase == KitchenPhase.SEATED,
                    onFood = { food ->
                        phase = KitchenPhase.EATING
                        message = "${pet.nickname ?: "Your pet"} is eating ${food.displayName.lowercase()}…"
                        when (onFeed(food, UUID.randomUUID().toString())) {
                            FeedDecision.Completed -> {
                                phase = KitchenPhase.CELEBRATING
                                message = "Happy and fed ♥ XP and Pet Coins earned."
                            }
                            FeedDecision.AlreadyCompleted -> {
                                phase = KitchenPhase.CELEBRATING
                                message = "That feeding was already completed."
                            }
                            FeedDecision.PetNotHungryEnough -> {
                                phase = KitchenPhase.SEATED
                                message = "They look full already. Try again when they ask for food."
                            }
                            FeedDecision.OutOfStock -> {
                                phase = KitchenPhase.SEATED
                                message = "You're out of that food."
                            }
                            FeedDecision.PetNotFound, FeedDecision.PetNotInKitchen -> {
                                phase = KitchenPhase.ROAMING
                                message = "The feeding couldn't start. Place the pet back in the Kitchen."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (phase == KitchenPhase.CELEBRATING) {
                Surface(
                    onClick = {
                        phase = KitchenPhase.ROAMING
                        message = "Press and hold to move them again."
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = EveluneRoseDeep,
                ) {
                    Text(
                        "Back to Kitchen",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 14.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodInventoryTray(
    inventory: Map<FoodId, Int>,
    enabled: Boolean,
    onFood: (FoodDefinition) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, color = EveluneCard, shadowElevation = 4.dp) {
        Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Food shelf",
                color = EveluneInk,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Text(
                "Choose food to feed the seated pet. Food is consumed only after a valid feeding starts.",
                color = EveluneMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                FoodCatalog.all.forEach { food ->
                    val count = inventory[food.id] ?: 0
                    item(food.id.name) {
                        Surface(
                            onClick = { if (enabled && count > 0) onFood(food) },
                            shape = RoundedCornerShape(18.dp),
                            color = if (count > 0) Color(0xFFFFF6FB) else Color(0xFFF1EDF1),
                            enabled = enabled && count > 0,
                        ) {
                            Column(
                                Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Text(foodEmoji(food.id), fontSize = 27.sp)
                                Text(food.displayName, color = EveluneInk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("×$count", color = EveluneMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KitchenScene(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFFFFEAF3), Color(0xFFFFF7F0), Color(0xFFF2D8C7))))
        drawRect(Color(0xFFFFFBF8), Offset(0f, size.height * .12f), Size(size.width, size.height * .45f))
        drawRect(Color(0xFFC99572), Offset(0f, size.height * .54f), Size(size.width, size.height * .035f))
        drawRect(Color(0xFFFFFDF9), Offset(size.width * .04f, size.height * .25f), Size(size.width * .52f, size.height * .30f))
        drawRoundRect(Color(0xFFF5AFC5), Offset(size.width * .70f, size.height * .18f), Size(size.width * .23f, size.height * .38f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()))
        drawRoundRect(Color(0xFFD5A17C), Offset(size.width * .10f, size.height * .64f), Size(size.width * .25f, size.height * .22f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()))
        drawRoundRect(Color(0xFFD5A17C), Offset(size.width * .65f, size.height * .64f), Size(size.width * .25f, size.height * .22f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()))
        drawCircle(Color(0xFFF5BED2), size.width * .055f, Offset(size.width * .50f, size.height * .25f))
    }
}

@Composable
private fun PetKitchenAvatar(
    species: PetSpecies,
    carried: Boolean,
    seated: Boolean,
    eating: Boolean,
) {
    val emoji = when (species) {
        PetSpecies.PUPPY -> "🐶"
        PetSpecies.KITTEN -> "🐱"
        PetSpecies.BUNNY -> "🐰"
        PetSpecies.DUCKLING -> "🐥"
        PetSpecies.HEDGEHOG -> "🦔"
        PetSpecies.FERRET -> "🐾"
        PetSpecies.OTTER -> "🦦"
        PetSpecies.FOX -> "🦊"
        PetSpecies.RED_PANDA -> "🐾"
        PetSpecies.AXOLOTL -> "🫧"
        PetSpecies.TIGER -> "🐯"
        PetSpecies.DRAGON -> "🐲"
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (eating) Text("🍽️", fontSize = 23.sp)
        Text(emoji, fontSize = if (carried) 66.sp else 62.sp)
        if (seated && !eating) Text("♡", color = EveluneRoseDeep, fontSize = 20.sp)
    }
}

private fun foodEmoji(food: FoodId): String = when (food) {
    FoodId.KIBBLE -> "🥣"
    FoodId.FISH -> "🐟"
    FoodId.CARROT -> "🥕"
    FoodId.BERRY_BOWL -> "🫐"
    FoodId.APPLE_SLICES -> "🍎"
    FoodId.MILK_BOWL -> "🥛"
    FoodId.BISCUIT -> "🦴"
    FoodId.SALAD_BOWL -> "🥗"
    FoodId.CUPCAKE_TREAT -> "🧁"
    FoodId.PREMIUM_FEAST_TRAY -> "🍱"
}

@Composable
private fun EmptyKitchenScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(EveluneBackground), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🍽️", fontSize = 50.sp)
            Text("The Kitchen is ready", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Hatch a pet first, then bring them here to eat.", color = EveluneMuted, fontSize = 14.sp)
            Surface(onClick = onBack, shape = RoundedCornerShape(20.dp), color = EveluneRoseDeep) {
                Text("Back to Pet World", color = Color.White, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
            }
        }
    }
}
