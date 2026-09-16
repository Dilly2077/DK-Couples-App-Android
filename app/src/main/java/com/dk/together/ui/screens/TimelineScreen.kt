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
import androidx.compose.material3.OutlinedButton
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
import com.dk.together.ui.AppUiState
import com.dk.together.ui.AppViewModel
import com.dk.together.ui.LabelPill
import com.dk.together.ui.SoftCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TimelineScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    var adding by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var detail by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy") }
    val dates = state.interactions.filter { it.type == "special_date" }
        .sortedBy { parseEpoch(it.body) ?: Long.MAX_VALUE }

    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 36.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Dates", style = MaterialTheme.typography.displaySmall)
                    Text("Only dates you add. Nothing is filled in for you.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = { adding = !adding }) { Text(if (adding) "Cancel" else "Add") }
            }
        }

        if (adding) {
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Add a special date", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Name") },
                        placeholder = { Text("First date, anniversary, trip…") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it; error = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Date") },
                        supportingText = { Text(error ?: "Use YYYY-MM-DD") },
                        isError = error != null,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = detail,
                        onValueChange = { detail = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Note · optional") },
                        minLines = 2
                    )
                    Button(
                        onClick = {
                            val date = runCatching { LocalDate.parse(dateText.trim()) }.getOrNull()
                            if (date == null) {
                                error = "Enter a valid date such as 2026-09-16"
                            } else if (title.isBlank()) {
                                error = "Give the date a name"
                            } else {
                                vm.addSpecialDate(title, date, detail)
                                title = ""
                                detail = ""
                                dateText = LocalDate.now().toString()
                                error = null
                                adding = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save date") }
                }
            }
        }

        if (dates.isEmpty()) {
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("♡", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                    Text("No dates yet", style = MaterialTheme.typography.headlineSmall)
                    Text("This stays empty until one of you adds something meaningful.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(dates, key = { it.id }) { item ->
                val pieces = item.body.split('|', limit = 2)
                val epoch = pieces.firstOrNull()?.toLongOrNull()
                val note = pieces.getOrNull(1).orEmpty()
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    LabelPill(epoch?.let { LocalDate.ofEpochDay(it).format(formatter) } ?: "Special date")
                    Text(item.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (note.isNotBlank()) Text(note, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Added by ${item.actor}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun parseEpoch(body: String): Long? = body.substringBefore('|').toLongOrNull()
