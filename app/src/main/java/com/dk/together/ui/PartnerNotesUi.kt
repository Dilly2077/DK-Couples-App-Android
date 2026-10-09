package com.dk.together.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dk.together.data.DuoNoteStore
import com.dk.together.data.LocalActor
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val noteTime = DateTimeFormatter.ofPattern("d MMM · HH:mm")

@Composable
internal fun PartnerNotesStrip(
    store: DuoNoteStore,
    actor: LocalActor,
) {
    var revision by remember { mutableIntStateOf(0) }
    var composing by remember { mutableStateOf(false) }
    val incoming = remember(actor, revision) { store.incomingFor(actor).take(10) }

    LaunchedEffect(actor, revision) {
        store.publishWidgetFor(actor)
    }

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Notes", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
            Text(
                if (incoming.isEmpty()) " · nothing from your partner yet" else " · latest from your partner",
                style = MaterialTheme.typography.labelMedium,
                color = EveluneMuted,
            )
        }

        LazyRow(
            contentPadding = PaddingValues(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Surface(
                    onClick = { composing = true },
                    modifier = Modifier.width(138.dp).heightIn(min = 86.dp),
                    color = EveluneRosePale,
                    shape = RoundedCornerShape(23.dp),
                ) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = EveluneRoseDeep)
                        Text("Leave a note", fontWeight = FontWeight.SemiBold, color = EveluneInk)
                    }
                }
            }

            items(incoming, key = { it.id }) { note ->
                Surface(
                    modifier = Modifier.width(246.dp).heightIn(min = 86.dp),
                    color = Color(0xFFFFFCFB),
                    shape = RoundedCornerShape(23.dp),
                    shadowElevation = 1.dp,
                ) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            note.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = EveluneInk,
                            maxLines = 3,
                        )
                        val stamp = Instant.ofEpochMilli(note.sentAt).atZone(ZoneId.systemDefault()).format(noteTime)
                        Text("${note.sender.display} · $stamp", style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
                    }
                }
            }
        }
    }

    if (composing) {
        NoteComposer(
            actor = actor,
            onDismiss = { composing = false },
            onSend = { text ->
                store.send(actor, text)
                composing = false
                revision++
            },
        )
    }
}

@Composable
private fun NoteComposer(
    actor: LocalActor,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFFFCFB),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 8.dp,
        ) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Leave a note", style = MaterialTheme.typography.headlineSmall, color = EveluneInk)
                Text(
                    "Sending as ${actor.display}. In the current single-phone test mode, switch to the other partner in Us to preview their side.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EveluneMuted,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 500) text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    shape = RoundedCornerShape(20.dp),
                    placeholder = { Text("Write something for them…") },
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EveluneRosePale, contentColor = EveluneRoseDeep),
                    ) { Text("Cancel") }
                    Button(
                        onClick = { onSend(text) },
                        enabled = text.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EveluneRoseDeep),
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null)
                        Box(Modifier.padding(start = 7.dp)) { Text("Send") }
                    }
                }
            }
        }
    }
}
