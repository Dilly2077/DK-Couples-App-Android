package com.dk.together.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import com.dk.together.ui.SoftCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun UsScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    val p = state.prefs
    val days = RelationshipMath.daysTogether(p.profile.startEpochDay)
    val start = LocalDate.ofEpochDay(p.profile.startEpochDay).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
    val actor = vm.currentActor(p)
    val other = vm.otherActor(p)

    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 36.dp)
    ) {
        item {
            Text("Us", style = MaterialTheme.typography.displaySmall)
            Text("Your shared space, kept deliberately simple.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${p.profile.youName} + ${p.profile.partnerName}", style = MaterialTheme.typography.headlineSmall)
                        Text("Together since $start", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LabelPill("$days days")
                }
            }
        }

        item {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Text("Two-side testing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "This build still stores both sides on one phone while the real account/pairing backend is being built. Use this switch to test the locked-answer and reveal flow exactly as each partner would see it.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("You are currently using the app as $actor.")
                Button(onClick = vm::toggleDemoPartner, modifier = Modifier.fillMaxWidth()) {
                    Text("Switch to $other")
                }
            }
        }

        item {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Text("Content library", style = MaterialTheme.typography.titleLarge)
                Text("${vm.questions.size} original couple questions")
                Text("${vm.cards.size} conversation cards")
                Text("${vm.gamePrompts.size} two-person game dilemmas")
                Text("No premium locks or paid decks.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
