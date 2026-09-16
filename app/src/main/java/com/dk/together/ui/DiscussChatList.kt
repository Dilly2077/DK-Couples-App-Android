package com.dk.together.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.content.EveluneContentBank
import com.dk.together.data.EveluneSocialStore
import com.dk.together.data.EveluneStore
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val discussDateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
private fun discussDate(epoch: Long): String = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).format(discussDateFormatter)

private data class DiscussRowModel(
    val type: String,
    val id: String,
    val title: String,
    val preview: String,
    val stamp: Long,
    val unlocked: Boolean,
)

@Composable
internal fun DiscussChatList(
    store: EveluneStore,
    social: EveluneSocialStore,
    refreshKey: Int,
    onOpen: (ContentDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = remember(refreshKey) {
        val submissions = store.allSubmissions()
            .filter { it.contentType == EveluneStore.TYPE_QUESTION || it.contentType == EveluneStore.TYPE_CARD }
            .groupBy { it.contentType to it.contentId }

        submissions.map { (key, values) ->
            val type = key.first
            val id = key.second
            val pair = store.pair(id, type)
            val question = if (type == EveluneStore.TYPE_QUESTION) EveluneContentBank.questions.firstOrNull { it.id == id } else null
            val card = if (type == EveluneStore.TYPE_CARD) EveluneContentBank.cardGames.firstOrNull { it.id == id } else null
            val latestChat = if (question != null) social.latestMessage(id) else null
            val stamp = maxOf(values.maxOfOrNull { it.submittedAt } ?: 0L, latestChat?.sentAt ?: 0L)
            val preview = when {
                !pair.bothSubmitted -> "Waiting for the other answer · still private"
                latestChat != null -> latestChat.text
                question != null -> "Both answers unlocked · open the conversation"
                else -> "Comparison unlocked"
            }
            DiscussRowModel(
                type = type,
                id = id,
                title = question?.prompt ?: card?.title ?: id,
                preview = preview,
                stamp = stamp,
                unlocked = pair.bothSubmitted,
            )
        }.sortedByDescending { it.stamp }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Discuss", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = EveluneRoseDeep)
                Text("Each answered question becomes its own conversation.", style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
            }
        }

        if (rows.isEmpty()) {
            item {
                Surface(color = EveluneCard, shape = RoundedCornerShape(24.dp)) {
                    Text(
                        "Answer a question or card and it will appear here.",
                        Modifier.padding(18.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = EveluneMuted,
                    )
                }
            }
        }

        items(rows, key = { "${it.type}|${it.id}" }) { row ->
            Surface(
                onClick = { onOpen(ContentDestination(row.type, row.id)) },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFFCFB),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 1.dp,
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(shape = CircleShape, color = EveluneRosePale) {
                        Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                if (row.type == EveluneStore.TYPE_QUESTION) Icons.Filled.Chat else Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = EveluneRoseDeep,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (row.type == EveluneStore.TYPE_QUESTION) "QUESTION CHAT" else "CARD RESULTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = EveluneRoseDeep,
                        )
                        Text(
                            row.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = EveluneInk,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            row.preview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EveluneMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (row.stamp > 0) {
                        Text(discussDate(row.stamp), style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
                    }
                }
            }
        }
    }
}
