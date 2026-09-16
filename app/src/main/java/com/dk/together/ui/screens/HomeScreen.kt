package com.dk.together.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dk.together.model.CardContent
import com.dk.together.model.GamePrompt
import com.dk.together.model.RelationshipMath
import com.dk.together.ui.AccentCard
import com.dk.together.ui.AppUiState
import com.dk.together.ui.AppViewModel
import com.dk.together.ui.LabelPill
import com.dk.together.ui.SectionTitle
import com.dk.together.ui.SoftCard
import com.dk.together.ui.StickyNoteCard

@Composable
fun HomeScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    val p = state.prefs
    val actor = vm.currentActor(p)
    val other = vm.otherActor(p)
    val days = RelationshipMath.daysTogether(p.profile.startEpochDay)
    var note by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 36.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Today", style = MaterialTheme.typography.displaySmall)
                    Text(
                        "${p.profile.youName} + ${p.profile.partnerName} · $days days",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LabelPill(if (p.demoAsPartner) "Testing as $actor" else "Together")
            }
        }

        item {
            val received = state.interactions.firstOrNull { it.type == "note" && it.actor == other }
            SectionTitle("On your home screen", "Sticky notes are meant to be glanceable, not another chat inbox.")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StickyNoteCard(modifier = Modifier.fillMaxWidth()) {
                    Text(if (received == null) "No note from $other yet" else "From $other", fontWeight = FontWeight.Bold)
                    Text(
                        received?.body ?: "When $other sends a note, it will appear here and on the couple widget.",
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = if (received == null) FontStyle.Italic else FontStyle.Normal
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Leave $other a note…") },
                    supportingText = { Text("${note.length}/120") },
                    minLines = 2,
                    maxLines = 3
                )
                Button(
                    onClick = { vm.sendNote(note); note = "" },
                    enabled = note.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Put note on the widget") }
            }
        }

        item { SectionTitle("Today’s connection", "One question, three cards, one five-round game.") }
        item { DailyQuestion(state, vm) }
        item { DailyCards(state, vm) }
        item { DailyGuessGame(state, vm) }
    }
}

@Composable
private fun DailyQuestion(state: AppUiState, vm: AppViewModel) {
    val p = state.prefs
    val actor = vm.currentActor(p)
    val other = vm.otherActor(p)
    val key = vm.dailyQuestionKey()
    val mine = vm.response("daily_answer", key, actor)
    val theirs = vm.response("daily_answer", key, other)
    var answer by remember(key, actor) { mutableStateOf("") }

    AccentCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LabelPill("Daily question")
            Text(vm.dailyQuestion.category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(vm.dailyQuestion.prompt, style = MaterialTheme.typography.headlineSmall)

        when {
            mine == null -> {
                if (theirs != null) Text("$other has answered · their answer stays locked until you answer.", color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Your private answer…") },
                    minLines = 3
                )
                Button(onClick = { vm.answerDaily(answer); answer = "" }, enabled = answer.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                    Text("Lock in my answer")
                }
            }
            theirs == null -> {
                LabelPill("Answer locked")
                Text(mine.body)
                Text("Waiting for $other. Their answer will reveal only after both of you reply.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                LabelPill("Both answered")
                AnswerReveal(actor, mine.body)
                AnswerReveal(other, theirs.body)
            }
        }
    }
}

@Composable
private fun DailyCards(state: AppUiState, vm: AppViewModel) {
    val p = state.prefs
    val actor = vm.currentActor(p)
    val other = vm.otherActor(p)
    val cards = vm.dailyCards
    var index by remember { mutableIntStateOf(0) }
    val card = cards[index.coerceIn(0, cards.lastIndex)]
    val key = vm.cardKey(card)
    val mine = vm.response("card_answer", key, actor)
    val theirs = vm.response("card_answer", key, other)
    var answer by remember(key, actor) { mutableStateOf("") }

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Daily cards", style = MaterialTheme.typography.titleLarge)
                Text("Card ${index + 1} of ${cards.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LabelPill(card.deck)
        }
        Text(card.prompt, style = MaterialTheme.typography.headlineSmall)

        when {
            mine == null -> {
                if (theirs != null) Text("$other has played this card. Their response is locked.", color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Your answer…") },
                    minLines = 2
                )
                Button(onClick = { vm.answerCard(card, answer); answer = "" }, enabled = answer.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                    Text("Play card")
                }
            }
            theirs == null -> {
                LabelPill("Played")
                Text(mine.body)
                Text("Waiting for $other before the card flips.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                AnswerReveal(actor, mine.body)
                AnswerReveal(other, theirs.body)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { index = (index - 1 + cards.size) % cards.size }, modifier = Modifier.weight(1f)) { Text("Previous") }
            FilledTonalButton(onClick = { index = (index + 1) % cards.size }, modifier = Modifier.weight(1f)) { Text("Next card") }
        }
    }
}

@Composable
private fun DailyGuessGame(state: AppUiState, vm: AppViewModel) {
    val p = state.prefs
    val actor = vm.currentActor(p)
    val other = vm.otherActor(p)
    val prompts = vm.dailyGame
    val myAnswered = prompts.count { vm.hasResponse("game_pick", vm.gameKey(it), actor) }
    val otherAnswered = prompts.count { vm.hasResponse("game_pick", vm.gameKey(it), other) }
    val current: GamePrompt? = prompts.firstOrNull { !vm.hasResponse("game_pick", vm.gameKey(it), actor) }
    var ownChoice by remember(current?.id, actor) { mutableStateOf("") }
    var partnerGuess by remember(current?.id, actor) { mutableStateOf("") }

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Guess Me", style = MaterialTheme.typography.titleLarge)
                Text("5 rounds · choose for yourself, then predict $other", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LabelPill("$myAnswered/${prompts.size}")
        }

        if (current != null) {
            Text("Which would you choose?", fontWeight = FontWeight.Bold)
            ChoiceRow(current, ownChoice) { ownChoice = it }
            Text("What do you think $other would choose?", fontWeight = FontWeight.Bold)
            ChoiceRow(current, partnerGuess) { partnerGuess = it }
            Button(
                onClick = { vm.answerGame(current, ownChoice, partnerGuess); ownChoice = ""; partnerGuess = "" },
                enabled = ownChoice.isNotBlank() && partnerGuess.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Lock round ${myAnswered + 1}") }
        } else if (otherAnswered < prompts.size) {
            LabelPill("Your five picks are locked")
            Text("$other has completed $otherAnswered/${prompts.size}. Results stay hidden until both of you finish.")
        } else {
            val mine = scoreFor(actor, other, prompts, vm)
            val theirs = scoreFor(other, actor, prompts, vm)
            LabelPill("Results unlocked")
            Text("You knew $other: $mine/${prompts.size}", style = MaterialTheme.typography.headlineSmall)
            Text("$other knew you: $theirs/${prompts.size}", style = MaterialTheme.typography.titleLarge)
            Text("Open Discuss to compare each round.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChoiceRow(prompt: GamePrompt, selected: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (selected == "A") {
            Button(onClick = { onSelect("A") }, modifier = Modifier.weight(1f)) { Text(prompt.optionA) }
        } else {
            OutlinedButton(onClick = { onSelect("A") }, modifier = Modifier.weight(1f)) { Text(prompt.optionA) }
        }
        if (selected == "B") {
            Button(onClick = { onSelect("B") }, modifier = Modifier.weight(1f)) { Text(prompt.optionB) }
        } else {
            OutlinedButton(onClick = { onSelect("B") }, modifier = Modifier.weight(1f)) { Text(prompt.optionB) }
        }
    }
}

@Composable
private fun AnswerReveal(name: String, answer: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(answer, style = MaterialTheme.typography.bodyLarge)
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
