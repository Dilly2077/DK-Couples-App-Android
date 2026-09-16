package com.dk.together.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dk.together.model.RelationshipMath
import com.dk.together.ui.AppUiState
import com.dk.together.ui.AppViewModel
import com.dk.together.ui.LabelPill
import com.dk.together.ui.SectionTitle
import com.dk.together.ui.SoftCard
import com.dk.together.ui.StatRow
import java.text.NumberFormat

@Composable
fun UsScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    val p = state.prefs
    val days = RelationshipMath.daysTogether(p.profile.startEpochDay)
    val (years, months, remDays) = RelationshipMath.relationshipBreakdown(p.profile.startEpochDay)
    val beats = RelationshipMath.estimatedHeartbeats(days)
    val questions = state.interactions.count { it.type == "question" }
    val memories = state.interactions.count { it.type == "memory" }
    val challenges = state.interactions.count { it.type == "challenge" }
    val petMoments = state.interactions.count { it.type == "pet" }
    val notes = state.interactions.count { it.type == "note" }

    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 28.dp)
    ) {
        item {
            Text("Us", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            Text("${p.profile.youName} + ${p.profile.partnerName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            SoftCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    BigCounter(years.toString(), "Years")
                    BigCounter(months.toString(), "Months")
                    BigCounter(remDays.toString(), "Days")
                }
                Text(
                    "Approximately ${NumberFormat.getIntegerInstance().format(beats)} heartbeats since you got together ✦",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Playful estimate using 70 beats per minute — not a medical measurement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionTitle("Insights", "Observations from what you do in the app, not relationship diagnoses.") }
        item {
            SoftCard {
                val observation = when {
                    questions >= 10 -> "You've built a solid question habit: $questions answers are now part of your history."
                    notes >= 5 -> "Small check-ins are becoming a pattern: you've sent $notes widget notes."
                    petMoments >= 5 -> "You keep returning to shared play: ${p.petName} has $petMoments care moments logged."
                    else -> "Your shared space is still new. A few more activities will make this section more personal."
                }
                LabelPill("This week")
                Text(observation, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Insights stay descriptive and never score the quality of your relationship.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item { SectionTitle("Our numbers") }
        item {
            SoftCard {
                StatRow("Days together", NumberFormat.getIntegerInstance().format(days), "♡")
                StatRow("Questions answered", questions.toString(), "?")
                StatRow("Challenges / games", challenges.toString(), "⚡")
                StatRow("Memories saved", memories.toString(), "▣")
                StatRow("Widget notes", notes.toString(), "✎")
                StatRow("Shared hearts", p.hearts.toString(), "♥")
            }
        }

        item { SectionTitle("Astrology · optional entertainment") }
        item {
            SoftCard {
                Text("Star-sign compatibility cards are reserved for the next content pass.", fontWeight = FontWeight.SemiBold)
                Text("When added, astrology will be clearly separated from app-activity insights and labelled for entertainment only.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item { SectionTitle("Home-screen widget") }
        item {
            SoftCard {
                Text("Add the DK Together widget from your Android launcher to see your relationship counter and latest note without opening the app.")
                Text("The widget is already registered in this build.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BigCounter(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
