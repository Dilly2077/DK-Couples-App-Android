package com.dk.together.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.content.EveluneContentBank
import com.dk.together.data.EveluneSocialStore
import com.dk.together.data.EveluneStore
import com.dk.together.data.LocalActor
import com.dk.together.data.Submission
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val chatTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale.getDefault())
private fun chatTime(epoch: Long): String = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).format(chatTimeFormatter)

@Composable
internal fun QuestionChatScreen(
    questionId: String,
    store: EveluneStore,
    social: EveluneSocialStore,
    actor: LocalActor,
    refreshKey: Int,
    onChanged: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = EveluneContentBank.questions.firstOrNull { it.id == questionId } ?: return
    var localRefresh by remember { mutableIntStateOf(0) }
    val pair = remember(refreshKey, localRefresh, actor, questionId) {
        store.pair(questionId, EveluneStore.TYPE_QUESTION)
    }
    val mine = if (actor == LocalActor.YOU) pair.you else pair.partner
    val initialSubmissions = remember(pair.you, pair.partner) {
        listOfNotNull(pair.you, pair.partner).sortedBy { it.submittedAt }
    }
    val thread = remember(refreshKey, localRefresh, questionId) { social.messages(questionId) }
    var answer by remember(questionId, actor, mine?.submittedAt) { mutableStateOf(mine?.payload.orEmpty()) }
    var message by remember(questionId, actor) { mutableStateOf("") }
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier.fillMaxSize().background(EveluneBackground).statusBarsPadding().imePadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EveluneRoseDeep)
            }
            Column(Modifier.weight(1f)) {
                Text("Question chat", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
                Text(question.category, style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
            }
            Surface(shape = RoundedCornerShape(18.dp), color = EveluneRosePale) {
                Text(
                    "As ${actor.display}",
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = EveluneRoseDeep,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    question.prompt,
                    fontFamily = FontFamily.Serif,
                    fontSize = 29.sp,
                    lineHeight = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = EveluneInk,
                )
            }

            if (!pair.bothSubmitted) {
                item {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        placeholder = { Text("Write your answer") },
                    )
                }
                item {
                    Button(
                        onClick = {
                            store.save(question.id, EveluneStore.TYPE_QUESTION, actor, answer.trim())
                            localRefresh++
                            onChanged()
                        },
                        enabled = answer.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EveluneRoseDeep),
                    ) {
                        Text(if (mine == null) "Submit answer" else "Update answer")
                    }
                }
            }

            if (initialSubmissions.isNotEmpty()) {
                item {
                    Text("Conversation", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
                }
                items(initialSubmissions, key = { "initial-${it.actor.key}" }) { submission ->
                    if (pair.bothSubmitted || submission.actor == actor) {
                        AnswerBubble(
                            submission = submission,
                            currentActor = actor,
                            partnerName = store.partnerName,
                        )
                    }
                }
            }

            if (initialSubmissions.isNotEmpty() && !pair.bothSubmitted) {
                item {
                    Surface(color = EveluneRosePale, shape = RoundedCornerShape(20.dp)) {
                        Text(
                            "Waiting for the other answer. Your response stays private until both partners submit.",
                            Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = EveluneMuted,
                        )
                    }
                }
            }

            if (pair.bothSubmitted) {
                items(thread, key = { "chat-${it.id}" }) { entry ->
                    MessageBubble(
                        actor = entry.actor,
                        currentActor = actor,
                        partnerName = store.partnerName,
                        text = entry.text,
                        at = entry.sentAt,
                    )
                }
            }
        }

        if (pair.bothSubmitted) {
            Row(
                Modifier.fillMaxWidth().background(Color(0xFFFFFCFA)).padding(horizontal = 12.dp, vertical = 9.dp).navigationBarsPadding(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    minLines = 1,
                    maxLines = 4,
                    shape = RoundedCornerShape(22.dp),
                    placeholder = { Text("Continue the conversation…") },
                )
                Surface(
                    onClick = {
                        if (message.isNotBlank()) {
                            social.addMessage(questionId, actor, message.trim())
                            message = ""
                            localRefresh++
                            onChanged()
                        }
                    },
                    enabled = message.isNotBlank(),
                    shape = CircleShape,
                    color = if (message.isNotBlank()) EveluneRoseDeep else EveluneRosePale,
                ) {
                    Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = if (message.isNotBlank()) Color.White else EveluneMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerBubble(submission: Submission, currentActor: LocalActor, partnerName: String) {
    ChatBubble(
        actor = submission.actor,
        currentActor = currentActor,
        partnerName = partnerName,
        text = submission.payload,
        at = submission.submittedAt,
        label = "Answer",
    )
}

@Composable
private fun MessageBubble(
    actor: LocalActor,
    currentActor: LocalActor,
    partnerName: String,
    text: String,
    at: Long,
) {
    ChatBubble(actor, currentActor, partnerName, text, at, label = null)
}

@Composable
private fun ChatBubble(
    actor: LocalActor,
    currentActor: LocalActor,
    partnerName: String,
    text: String,
    at: Long,
    label: String?,
) {
    val mine = actor == currentActor
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            if (mine) "You" else partnerName,
            style = MaterialTheme.typography.labelMedium,
            color = EveluneMuted,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        Surface(
            color = if (mine) EveluneRoseDeep else Color(0xFFFFFCFB),
            shape = if (mine) RoundedCornerShape(22.dp, 22.dp, 6.dp, 22.dp) else RoundedCornerShape(22.dp, 22.dp, 22.dp, 6.dp),
            shadowElevation = if (mine) 0.dp else 1.dp,
        ) {
            Column(
                Modifier.widthIn(max = 310.dp).padding(horizontal = 15.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                if (label != null) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (mine) Color.White.copy(alpha = .72f) else EveluneRoseDeep,
                    )
                }
                Text(text, style = MaterialTheme.typography.bodyLarge, color = if (mine) Color.White else EveluneInk)
                Text(
                    chatTime(at),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (mine) Color.White.copy(alpha = .68f) else EveluneMuted,
                )
            }
        }
    }
}
