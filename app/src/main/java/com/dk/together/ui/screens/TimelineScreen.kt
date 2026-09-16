package com.dk.together.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import com.dk.together.ui.AppUiState
import com.dk.together.ui.AppViewModel
import com.dk.together.ui.LabelPill
import com.dk.together.ui.SectionTitle
import com.dk.together.ui.SoftCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TimelineScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    val formatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm") }

    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 28.dp)
    ) {
        item {
            Text("Timeline", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            Text("Questions, notes, pet moments and memories in one private history.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item { SectionTitle("Add a memory") }
        item {
            SoftCard {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Memory title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("What happened?") },
                    minLines = 2
                )
                Button(
                    onClick = { vm.addMemory(title, detail); title = ""; detail = "" },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save memory · +3 hearts") }
            }
        }

        item { SectionTitle("Our history") }
        if (state.interactions.isEmpty()) {
            item { Text("Nothing here yet. Answer a question or save your first memory.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(state.interactions, key = { it.id }) { entry ->
                SoftCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LabelPill(entry.type.replaceFirstChar { it.uppercase() })
                        val time = Instant.ofEpochMilli(entry.createdAt).atZone(ZoneId.systemDefault()).format(formatter)
                        Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (entry.body.isNotBlank()) Text(entry.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(entry.actor, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
