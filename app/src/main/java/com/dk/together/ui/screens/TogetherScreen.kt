package com.dk.together.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dk.together.model.GamePrompt
import com.dk.together.ui.AppUiState
import com.dk.together.ui.AppViewModel
import com.dk.together.ui.LabelPill
import com.dk.together.ui.SoftCard
import com.dk.together.ui.StickyNoteCard

@Composable
fun TogetherScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf("Answers") }
    val you = state.prefs.profile.youName
    val partner = state.prefs.profile.partnerName

    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 36.dp)
    ) {
        item {
            Text("Discuss", style = MaterialTheme.typography.displaySmall)
            Text("The things you have both unlocked, without the rest of the app getting in the way.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Answers", "Notes", "Game").forEach { item ->
                    FilterChip(selected = mode == item, onClick = { mode = item }, label = { Text(item) }, modifier = Modifier.weight(1f))
                }
            }
        }

        when (mode) {
            "Notes" -> notes(state)
            "Game" -> gameResults(state, vm)
            else -> answers(state, vm, you, partner)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.notes(state: AppUiState) {
    val notes = state.interactions.filter { it.type == "note" }
    if (notes.isEmpty()) {
        item { EmptyDiscussCard("No sticky notes yet", "Notes you send from Home will collect here.") }
    } else {
        items(notes, key = { it.id }) { note ->
            StickyNoteCard(modifier = Modifier.fillMaxWidth()) {
                Text(note.actor, fontWeight = FontWeight.Bold)
                Text(note.body, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.answers(
    state: AppUiState,
    vm: AppViewModel,
    you: String,
    partner: String
) {
    val grouped = state.interactions
        .filter { it.type == "daily_answer" || it.type == "card_answer" }
        .groupBy { "${it.type}|${it.title}" }
        .values
        .filter { group -> group.any { it.actor == you } && group.any { it.actor == partner } }
        .sortedByDescending { group -> group.maxOf { it.createdAt } }

    if (grouped.isEmpty()) {
        item {
            EmptyDiscussCard(
                "Nothing revealed yet",
                "An answer only appears here after both of you have answered the same question or card."
            )
        }
    } else {
        items(grouped, key = { group -> "${group.first().type}:${group.first().title}" }) { group ->
            val first = group.first()
            val prompt = if (first.type == "daily_answer") vm.questionByKey(first.title)?.prompt else vm.cardByKey(first.title)?.prompt
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                LabelPill(if (first.type == "daily_answer") "Question" else "Card")
                if (prompt != null) Text(prompt, style = MaterialTheme.typography.titleLarge)
                RevealLine(you, group.first { it.actor == you }.body)
                RevealLine(partner, group.first { it.actor == partner }.body)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.gameResults(state: AppUiState, vm: AppViewModel) {
    val you = state.prefs.profile.youName
    val partner = state.prefs.profile.partnerName
    val prompts = vm.dailyGame
    val completed = prompts.all { prompt ->
        val key = vm.gameKey(prompt)
        vm.response("game_pick", key, you) != null && vm.response("game_pick", key, partner) != null
    }

    if (!completed) {
        item {
            val youDone = prompts.count { vm.response("game_pick", vm.gameKey(it), you) != null }
            val partnerDone = prompts.count { vm.response("game_pick", vm.gameKey(it), partner) != null }
            EmptyDiscussCard("Today’s game is still locked", "$you: $youDone/5 · $partner: $partnerDone/5")
        }
    } else {
        val youScore = scoreFor(you, partner, prompts, vm)
        val partnerScore = scoreFor(partner, you, prompts, vm)
        item {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                LabelPill("Guess Me · results")
                Text("$you knew $partner: $youScore/5", style = MaterialTheme.typography.headlineSmall)
                Text("$partner knew $you: $partnerScore/5", style = MaterialTheme.typography.titleLarge)
            }
        }
        items(prompts, key = { it.id }) { prompt ->
            val youPick = vm.response("game_pick", vm.gameKey(prompt), you)!!
            val partnerPick = vm.response("game_pick", vm.gameKey(prompt), partner)!!
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                LabelPill(prompt.category)
                Text("A · ${prompt.optionA}")
                Text("B · ${prompt.optionB}")
                Text("$you chose ${gameValue(youPick.body, "own")} · guessed ${gameValue(youPick.body, "guess")}", fontWeight = FontWeight.SemiBold)
                Text("$partner chose ${gameValue(partnerPick.body, "own")} · guessed ${gameValue(partnerPick.body, "guess")}", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RevealLine(name: String, answer: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(answer, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun EmptyDiscussCard(title: String, body: String) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun scoreFor(actor: String, other: String, prompts: List<GamePrompt>, vm: AppViewModel): Int =
    prompts.count { prompt ->
        val mine = vm.response("game_pick", vm.gameKey(prompt), actor)?.body ?: return@count false
        val theirs = vm.response("game_pick", vm.gameKey(prompt), other)?.body ?: return@count false
        gameValue(mine, "guess") == gameValue(theirs, "own")
    }

private fun gameValue(body: String, key: String): String =
    body.split(';').firstOrNull { it.startsWith("$key=") }?.substringAfter('=') ?: ""
