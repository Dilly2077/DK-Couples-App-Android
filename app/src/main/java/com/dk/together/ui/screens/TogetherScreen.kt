package com.dk.together.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.model.PetMath
import com.dk.together.model.PetStats
import com.dk.together.ui.AccentCard
import com.dk.together.ui.AppUiState
import com.dk.together.ui.AppViewModel
import com.dk.together.ui.LabelPill
import com.dk.together.ui.SectionTitle
import com.dk.together.ui.SoftCard

private val petRooms = listOf("Living room", "Kitchen", "Bathroom", "Outdoors")

@Composable
fun TogetherScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    var note by remember { mutableStateOf("") }
    val p = state.prefs
    val actor = if (p.demoAsPartner) p.profile.partnerName else p.profile.youName
    val partnerMood = if (p.demoAsPartner) p.mood else p.partnerMood
    val partnerStatus = if (p.demoAsPartner) p.status else p.partnerStatus

    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 30.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Together", style = MaterialTheme.typography.displaySmall)
                    Text("Your shared little world.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LabelPill("${p.hearts} ♥")
            }
        }

        item {
            SoftCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(if (p.demoAsPartner) "Viewing as ${p.profile.partnerName}" else "${p.profile.partnerName} right now", fontWeight = FontWeight.SemiBold)
                        Text("$partnerMood · $partnerStatus", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = vm::toggleDemoPartner) {
                        Text(if (p.demoAsPartner) "Back to me" else "Partner mode")
                    }
                }
            }
        }

        item { SectionTitle("Pet world", "Both of you care for ${p.petName}. Needs slowly change over time and the pet reacts to what it needs.") }

        item {
            AccentCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(p.petName, style = MaterialTheme.typography.headlineSmall)
                        Text("Playful · curious · shared by two", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LabelPill(p.petRoom)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    petRooms.forEach { room ->
                        FilterChip(
                            selected = p.petRoom == room,
                            onClick = { vm.setPetRoom(room) },
                            label = { Text(room) }
                        )
                    }
                }

                PetWorldScene(room = p.petRoom, name = p.petName, stats = p.pet)

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    PetNeed("Hunger", "🍓", p.pet.hunger)
                    PetNeed("Cleanliness", "🫧", p.pet.cleanliness)
                    PetNeed("Happiness", "✨", p.pet.happiness)
                    PetNeed("Energy", "💤", p.pet.energy)
                    PetNeed("Affection", "💜", p.pet.affection)
                }

                val actions = when (p.petRoom) {
                    "Kitchen" -> listOf("Feed", "Treat", "Cuddle")
                    "Bathroom" -> listOf("Wash", "Splash", "Cuddle")
                    "Outdoors" -> listOf("Explore", "Play", "Cuddle")
                    else -> listOf("Play", "Cuddle", "Nap")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    actions.forEachIndexed { index, action ->
                        if (index == 0) {
                            Button(onClick = { vm.petAction(action) }, modifier = Modifier.weight(1f)) { Text(action) }
                        } else {
                            OutlinedButton(onClick = { vm.petAction(action) }, modifier = Modifier.weight(1f)) { Text(action) }
                        }
                    }
                }
                Text(
                    "Every care action is added to your shared history so you can see who fed, washed or played with ${p.petName}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionTitle("Mood & status", "A quick signal for your partner, not another chat thread.") }
        item {
            var moodOpen by remember { mutableStateOf(false) }
            var statusOpen by remember { mutableStateOf(false) }
            val moods = listOf("Happy", "Calm", "Loved", "Tired", "Stressed", "Missing you")
            val statuses = listOf("Free", "Working", "Studying", "Gym", "Gaming", "Busy")
            SoftCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { moodOpen = !moodOpen }, modifier = Modifier.weight(1f)) { Text("Mood · ${if (p.demoAsPartner) p.partnerMood else p.mood}") }
                    OutlinedButton(onClick = { statusOpen = !statusOpen }, modifier = Modifier.weight(1f)) { Text("Status · ${if (p.demoAsPartner) p.partnerStatus else p.status}") }
                }
                if (moodOpen) {
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        moods.forEach { mood -> FilterChip(selected = false, onClick = { vm.setMood(mood); moodOpen = false }, label = { Text(mood) }) }
                    }
                }
                if (statusOpen) {
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        statuses.forEach { status -> FilterChip(selected = false, onClick = { vm.setStatus(status); statusOpen = false }, label = { Text(status) }) }
                    }
                }
            }
        }

        item { SectionTitle("Home-screen note") }
        item {
            SoftCard {
                Text("“${p.widgetNote}”", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Send something small") },
                    singleLine = true
                )
                Button(
                    onClick = { vm.sendNote(note); note = "" },
                    enabled = note.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Update couple widget") }
            }
        }

        item { SectionTitle("Shared activity") }
        val messages = state.interactions.filter { it.type in setOf("note", "mood", "status", "pet", "game") }.take(10)
        if (messages.isEmpty()) {
            item { Text("Your shared interactions will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(messages, key = { it.id }) { entry ->
                SoftCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LabelPill(entry.type.replaceFirstChar { it.uppercase() })
                        Text(entry.actor, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(entry.title, fontWeight = FontWeight.SemiBold)
                    Text(entry.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PetWorldScene(room: String, name: String, stats: PetStats) {
    val transition = rememberInfiniteTransition(label = "pet-walk")
    val walk by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(5_200), repeatMode = RepeatMode.Reverse),
        label = "walk-position"
    )
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(650), repeatMode = RepeatMode.Reverse),
        label = "pet-bob"
    )

    val roomColor = when (room) {
        "Kitchen" -> Color(0xFF6F557B)
        "Bathroom" -> Color(0xFF4C6680)
        "Outdoors" -> Color(0xFF42645D)
        else -> Color(0xFF5A4A79)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(roomColor)
    ) {
        RoomDecor(room)
        val travel = (maxWidth - 98.dp) * walk
        Column(
            modifier = Modifier.offset(x = travel, y = (130 + bob * 5).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFDF8FF),
                shadowElevation = 3.dp
            ) {
                Text(
                    PetMath.thought(stats),
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    color = Color(0xFF2A2148),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.height(6.dp))
            PetSprite()
            Text(name, style = MaterialTheme.typography.labelMedium, color = Color.White)
        }
    }
}

@Composable
private fun RoomDecor(room: String) {
    Box(modifier = Modifier.fillMaxWidth().height(270.dp)) {
        when (room) {
            "Kitchen" -> {
                Text("🪴", fontSize = 32.sp, modifier = Modifier.align(Alignment.TopStart).padding(18.dp))
                Text("🍎  🥣", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomStart).padding(22.dp))
                Text("🧊", fontSize = 52.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(18.dp))
            }
            "Bathroom" -> {
                Text("🪞", fontSize = 46.sp, modifier = Modifier.align(Alignment.TopStart).padding(18.dp))
                Text("🛁", fontSize = 64.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))
                Text("🫧", fontSize = 36.sp, modifier = Modifier.align(Alignment.CenterStart).padding(28.dp))
            }
            "Outdoors" -> {
                Text("☁️", fontSize = 44.sp, modifier = Modifier.align(Alignment.TopEnd).padding(18.dp))
                Text("🌳", fontSize = 66.sp, modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))
                Text("🌷", fontSize = 38.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(30.dp))
            }
            else -> {
                Text("🪴", fontSize = 42.sp, modifier = Modifier.align(Alignment.TopEnd).padding(18.dp))
                Text("🛋️", fontSize = 66.sp, modifier = Modifier.align(Alignment.BottomStart).padding(18.dp))
                Text("🧸", fontSize = 36.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(28.dp))
            }
        }
    }
}

@Composable
private fun PetSprite() {
    Canvas(modifier = Modifier.size(76.dp)) {
        val purple = Color(0xFFB58BFF)
        val dark = Color(0xFF3A285E)
        val blush = Color(0xFFF5AFCB)
        val bodyCenter = Offset(size.width * .5f, size.height * .58f)
        drawCircle(purple, radius = size.minDimension * .31f, center = bodyCenter)

        val leftEar = Path().apply {
            moveTo(size.width * .25f, size.height * .39f)
            lineTo(size.width * .30f, size.height * .08f)
            lineTo(size.width * .46f, size.height * .33f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(size.width * .54f, size.height * .33f)
            lineTo(size.width * .70f, size.height * .08f)
            lineTo(size.width * .76f, size.height * .40f)
            close()
        }
        drawPath(leftEar, purple)
        drawPath(rightEar, purple)
        drawCircle(Color.White, radius = size.minDimension * .055f, center = Offset(size.width * .40f, size.height * .52f))
        drawCircle(Color.White, radius = size.minDimension * .055f, center = Offset(size.width * .60f, size.height * .52f))
        drawCircle(dark, radius = size.minDimension * .025f, center = Offset(size.width * .40f, size.height * .53f))
        drawCircle(dark, radius = size.minDimension * .025f, center = Offset(size.width * .60f, size.height * .53f))
        drawCircle(blush, radius = size.minDimension * .035f, center = Offset(size.width * .50f, size.height * .63f))
        drawCircle(purple, radius = size.minDimension * .10f, center = Offset(size.width * .28f, size.height * .82f))
        drawCircle(purple, radius = size.minDimension * .10f, center = Offset(size.width * .72f, size.height * .82f))
    }
}

@Composable
private fun PetNeed(label: String, emoji: String, value: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$emoji  $label", style = MaterialTheme.typography.bodyMedium)
            Text("$value%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
            color = when {
                value < 35 -> MaterialTheme.colorScheme.tertiary
                value < 60 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primary
            },
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .10f)
        )
    }
}
