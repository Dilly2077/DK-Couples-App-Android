package com.dk.together.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.dk.together.model.RelationshipMath
import com.dk.together.ui.AppUiState
import com.dk.together.ui.AppViewModel
import com.dk.together.ui.LabelPill
import com.dk.together.ui.SectionTitle
import com.dk.together.ui.SoftCard

@Composable
fun HomeScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    var answer by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val p = state.prefs
    val days = RelationshipMath.daysTogether(p.profile.startEpochDay)
    val recent = state.interactions.take(3)

    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 28.dp)
    ) {
        item {
            Text("Home", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            Text(
                "${p.profile.youName} + ${p.profile.partnerName} · $days days together",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            SoftCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${p.profile.partnerName}'s status", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${p.partnerMood} · ${p.partnerStatus}", fontWeight = FontWeight.SemiBold)
                    }
                    Text("💜", style = MaterialTheme.typography.headlineMedium)
                }
                Text("${p.petName} is waiting in your shared room 🐾")
            }
        }

        item {
            SectionTitle("Daily activities", "A small reason to check in with each other.")
        }

        item {
            SoftCard {
                LabelPill("Question · ${vm.dailyQuestion.category}")
                Text(vm.dailyQuestion.prompt, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Your answer") },
                    minLines = 2
                )
                Button(
                    onClick = {
                        vm.answerDaily(answer)
                        answer = ""
                    },
                    enabled = answer.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save answer · +5 hearts") }
            }
        }

        item {
            SoftCard {
                LabelPill("Widget note")
                Text("Leave something small on the shared home-screen widget.", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Thinking of you…") },
                    singleLine = true
                )
                Button(
                    onClick = {
                        vm.sendNote(note)
                        note = ""
                    },
                    enabled = note.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Send to widget") }
            }
        }

        item { SectionTitle("Recent moments") }
        if (recent.isEmpty()) {
            item { Text("Your answers, notes, pet moments and memories will collect here.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(recent, key = { it.id }) { entry ->
                SoftCard {
                    LabelPill(entry.type.replaceFirstChar { it.uppercase() })
                    Text(entry.title, fontWeight = FontWeight.SemiBold)
                    Text(entry.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("— ${entry.actor}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
