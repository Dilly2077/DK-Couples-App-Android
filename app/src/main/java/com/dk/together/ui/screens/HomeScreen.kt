package com.dk.together.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.model.PetMath
import com.dk.together.model.RelationshipMath
import com.dk.together.ui.AccentCard
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
        modifier = modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 30.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Home", style = MaterialTheme.typography.displaySmall)
                    Text("$days days together", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LabelPill("${p.hearts} ♥")
            }
        }

        item {
            AccentCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${p.profile.youName}  +  ${p.profile.partnerName}", style = MaterialTheme.typography.headlineSmall)
                        Text("${p.profile.partnerName}: ${p.partnerMood} · ${p.partnerStatus}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("💜", fontSize = 30.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SoftCard(modifier = Modifier.weight(1f)) {
                        Text("${p.petName} · ${p.petRoom}", fontWeight = FontWeight.SemiBold)
                        Text(PetMath.thought(p.pet), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SoftCard(modifier = Modifier.weight(1f)) {
                        Text("Widget note", fontWeight = FontWeight.SemiBold)
                        Text(p.widgetNote, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
            }
        }

        item { SectionTitle("Today", "One small thing to do together.") }
        item {
            SoftCard {
                LabelPill("Question · ${vm.dailyQuestion.category}")
                Text(vm.dailyQuestion.prompt, style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write your answer…") },
                    minLines = 2
                )
                Button(
                    onClick = { vm.answerDaily(answer); answer = "" },
                    enabled = answer.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save answer · +5 hearts") }
            }
        }

        item { SectionTitle("Send to their home screen") }
        item {
            SoftCard {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Thinking of you…") },
                    singleLine = true
                )
                Button(
                    onClick = { vm.sendNote(note); note = "" },
                    enabled = note.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Update couple widget") }
            }
        }

        item { SectionTitle("Recent moments") }
        if (recent.isEmpty()) {
            item { Text("Your answers, pet moments and memories will collect here.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(recent, key = { it.id }) { entry ->
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
