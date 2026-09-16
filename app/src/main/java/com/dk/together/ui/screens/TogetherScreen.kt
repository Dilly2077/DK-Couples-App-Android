package com.dk.together.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dk.together.ui.AppUiState
import com.dk.together.ui.AppViewModel
import com.dk.together.ui.LabelPill
import com.dk.together.ui.SectionTitle
import com.dk.together.ui.SoftCard

@Composable
fun TogetherScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    var note by remember { mutableStateOf("") }
    val p = state.prefs
    val actor = if (p.demoAsPartner) p.profile.partnerName else p.profile.youName
    val moods = listOf("Happy", "Calm", "Loved", "Tired", "Stressed", "Missing you")
    val statuses = listOf("Free", "Working", "Studying", "Gym", "Gaming", "Busy")

    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 28.dp)
    ) {
        item {
            Text("Together", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            Text("Shared status, widgets and your little world.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            SoftCard {
                Text("Demo partner mode", fontWeight = FontWeight.Bold)
                Text("Testing as $actor", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = vm::toggleDemoPartner, modifier = Modifier.fillMaxWidth()) {
                    Text(if (p.demoAsPartner) "Switch back to ${p.profile.youName}" else "Switch to ${p.profile.partnerName}")
                }
            }
        }

        item { SectionTitle("Mood & status") }
        item {
            SoftCard {
                Text("How is $actor feeling?", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    moods.take(3).forEach { mood -> OutlinedButton(onClick = { vm.setMood(mood) }, modifier = Modifier.weight(1f)) { Text(mood) } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    moods.drop(3).forEach { mood -> OutlinedButton(onClick = { vm.setMood(mood) }, modifier = Modifier.weight(1f)) { Text(mood) } }
                }
                Text("Status", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    statuses.take(3).forEach { s -> OutlinedButton(onClick = { vm.setStatus(s) }, modifier = Modifier.weight(1f)) { Text(s) } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    statuses.drop(3).forEach { s -> OutlinedButton(onClick = { vm.setStatus(s) }, modifier = Modifier.weight(1f)) { Text(s) } }
                }
            }
        }

        item { SectionTitle("Shared pet", "${p.petName} can never die; low needs only change how they behave.") }
        item {
            SoftCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("🐾 ${p.petName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Text("Playful · curious", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LabelPill("${p.hearts} ♥")
                }
                PetMeter("Hunger", p.pet.hunger)
                PetMeter("Happiness", p.pet.happiness)
                PetMeter("Cleanliness", p.pet.cleanliness)
                PetMeter("Energy", p.pet.energy)
                PetMeter("Affection", p.pet.affection)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Feed", "Play", "Bath").forEach { action -> Button(onClick = { vm.petAction(action) }, modifier = Modifier.weight(1f)) { Text(action) } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Nap", "Cuddle").forEach { action -> OutlinedButton(onClick = { vm.petAction(action) }, modifier = Modifier.weight(1f)) { Text(action) } }
                }
            }
        }

        item { SectionTitle("Widget note") }
        item {
            SoftCard {
                Text("Current: ${p.widgetNote}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("New note") }, singleLine = true)
                Button(onClick = { vm.sendNote(note); note = "" }, enabled = note.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Send") }
            }
        }

        item { SectionTitle("Message history") }
        val messages = state.interactions.filter { it.type in setOf("note", "mood", "status", "pet") }.take(12)
        if (messages.isEmpty()) {
            item { Text("Your shared interactions will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(messages, key = { it.id }) { entry ->
                SoftCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LabelPill(entry.type.replaceFirstChar { it.uppercase() })
                        Text(entry.actor, style = MaterialTheme.typography.labelMedium)
                    }
                    Text(entry.title, fontWeight = FontWeight.SemiBold)
                    Text(entry.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PetMeter(label: String, value: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text("$value%", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(progress = { value / 100f }, modifier = Modifier.fillMaxWidth())
    }
}
