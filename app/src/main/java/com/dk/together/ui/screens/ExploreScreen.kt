package com.dk.together.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
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
import com.dk.together.ui.AppUiState
import com.dk.together.ui.AppViewModel
import com.dk.together.ui.LabelPill
import com.dk.together.ui.SoftCard

@Composable
fun ExploreScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf("Questions") }
    var filter by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }

    val filters = when (mode) {
        "Cards" -> listOf("All") + vm.cards.map { it.deck }.distinct()
        "Games" -> listOf("All") + vm.gamePrompts.map { it.category }.distinct()
        else -> listOf("All") + vm.questions.map { it.category }.distinct()
    }

    if (filter !in filters) filter = "All"

    val questionResults = vm.questions.filter {
        (filter == "All" || it.category == filter) && (search.isBlank() || it.prompt.contains(search, ignoreCase = true))
    }
    val cardResults = vm.cards.filter {
        (filter == "All" || it.deck == filter) && (search.isBlank() || it.prompt.contains(search, ignoreCase = true))
    }
    val gameResults = vm.gamePrompts.filter {
        (filter == "All" || it.category == filter) &&
            (search.isBlank() || it.optionA.contains(search, true) || it.optionB.contains(search, true))
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 36.dp)
    ) {
        item {
            Text("Explore", style = MaterialTheme.typography.displaySmall)
            Text(
                "${vm.questions.size} questions · ${vm.cards.size} cards · ${vm.gamePrompts.size} game dilemmas",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Questions", "Cards", "Games").forEach { item ->
                    FilterChip(
                        selected = mode == item,
                        onClick = { mode = item; filter = "All" },
                        label = { Text(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search the library") },
                singleLine = true
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { item ->
                    FilterChip(selected = filter == item, onClick = { filter = item }, label = { Text(item) })
                }
            }
        }

        when (mode) {
            "Cards" -> {
                items(cardResults.take(60), key = { it.id }) { card ->
                    SoftCard {
                        LabelPill(card.deck)
                        Text(card.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (cardResults.size > 60) item { ResultHint(cardResults.size) }
            }
            "Games" -> {
                items(gameResults.take(60), key = { it.id }) { game ->
                    SoftCard {
                        LabelPill(game.category)
                        Text("A · ${game.optionA}", style = MaterialTheme.typography.titleMedium)
                        Text("B · ${game.optionB}", style = MaterialTheme.typography.titleMedium)
                    }
                }
                if (gameResults.size > 60) item { ResultHint(gameResults.size) }
            }
            else -> {
                items(questionResults.take(60), key = { it.id }) { question ->
                    SoftCard {
                        LabelPill(question.category)
                        Text(question.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (questionResults.size > 60) item { ResultHint(questionResults.size) }
            }
        }
    }
}

@Composable
private fun ResultHint(total: Int) {
    Text(
        "$total matches. Refine the category or search to narrow the list.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
