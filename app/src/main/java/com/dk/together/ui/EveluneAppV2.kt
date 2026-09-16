package com.dk.together.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.dk.together.content.DailyQuestion
import com.dk.together.content.EveluneContentBank
import com.dk.together.data.EveluneSocialStore
import com.dk.together.data.EveluneStore
import com.dk.together.data.LocalActor
import com.dk.together.data.MemoryMoment
import com.dk.together.data.QuestionMessage
import com.dk.together.data.Submission
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRose
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class V2Artwork { QUESTION, GAME, CARD, CHALLENGE }
private val v2DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale.getDefault())
private val v2DateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

private fun v2FormatTime(epoch: Long): String = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).format(v2DateTimeFormatter)
private fun v2FormatDate(epoch: Long): String = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).format(v2DateFormatter)

@Composable
fun EveluneAppV2() {
    val context = LocalContext.current
    val store = remember { EveluneStore(context) }
    val social = remember { EveluneSocialStore(context) }
    var selected by remember { mutableIntStateOf(0) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var actor by remember { mutableStateOf(store.currentActor) }
    var destination by remember { mutableStateOf<ContentDestination?>(null) }
    val labels = listOf("Home", "Explore", "Discuss", "Timeline", "Us")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = EveluneBackground,
        bottomBar = {
            if (destination == null) {
                BottomBarV2(selected, labels) { selected = it }
            }
        },
    ) { innerPadding ->
        val open = destination
        if (open != null) {
            if (open.type == EveluneStore.TYPE_QUESTION) {
                QuestionChatDetailV2(
                    questionId = open.id,
                    store = store,
                    social = social,
                    actor = actor,
                    refreshKey = refreshKey,
                    onChanged = { refreshKey++ },
                    onBack = { destination = null },
                    modifier = Modifier.padding(innerPadding),
                )
            } else {
                Box(Modifier.padding(innerPadding).statusBarsPadding()) {
                    ContentDetailUi(
                        destination = open,
                        store = store,
                        actor = actor,
                        refreshKey = refreshKey,
                        onSaved = { refreshKey++ },
                        onBack = { destination = null },
                    )
                }
            }
        } else {
            when (selected) {
                0 -> HomeV2(store, actor, refreshKey, { destination = it }, Modifier.padding(innerPadding))
                1 -> ExploreUi(store, actor, refreshKey, { destination = it }, Modifier.padding(innerPadding).statusBarsPadding())
                2 -> DiscussV2(store, social, refreshKey, { destination = it }, Modifier.padding(innerPadding).statusBarsPadding())
                3 -> TimelineV2(social, Modifier.padding(innerPadding).statusBarsPadding())
                else -> UsUi(
                    store = store,
                    actor = actor,
                    onActorChanged = {
                        actor = it
                        store.currentActor = it
                        refreshKey++
                    },
                    modifier = Modifier.padding(innerPadding).statusBarsPadding(),
                )
            }
        }
    }
}

@Composable
private fun HomeV2(
    store: EveluneStore,
    actor: LocalActor,
    refreshKey: Int,
    onOpen: (ContentDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = EveluneContentBank.todayQuestion()
    val card = EveluneContentBank.todayCard()
    val challenge = EveluneContentBank.todayChallenge()
    val qDone = remember(refreshKey, actor, question.id) { store.completedBy(question.id, EveluneStore.TYPE_QUESTION, actor) != null }
    val cDone = remember(refreshKey, actor, card.id) { store.completedBy(card.id, EveluneStore.TYPE_CARD, actor) != null }
    val chDone = remember(refreshKey, actor, challenge.id) { store.completedBy(challenge.id, EveluneStore.TYPE_CHALLENGE, actor) != null }

    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        HomeBackdropV2()
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            item { PartnerNoteV2() }
            item { HeroV2() }
            item {
                ActivityCardV2(
                    label = "DAILY QUESTION",
                    icon = "?",
                    title = question.prompt,
                    artwork = V2Artwork.QUESTION,
                    completed = qDone,
                ) { onOpen(ContentDestination(EveluneStore.TYPE_QUESTION, question.id)) }
            }
            item {
                ActivityCardV2(
                    label = "DAILY GAME",
                    icon = "✦",
                    title = "Pixel games",
                    subtitle = "Coming in the next stage.",
                    artwork = V2Artwork.GAME,
                    completed = false,
                    onClick = null,
                )
            }
            item {
                ActivityCardV2(
                    label = "DAILY CARD",
                    icon = "□",
                    title = card.title,
                    subtitle = "4 prompts · rate 1–4",
                    artwork = V2Artwork.CARD,
                    completed = cDone,
                ) { onOpen(ContentDestination(EveluneStore.TYPE_CARD, card.id)) }
            }
            item {
                ActivityCardV2(
                    label = "DAILY CHALLENGE",
                    icon = "◎",
                    title = EveluneContentBank.render(challenge.prompt, store.partnerName),
                    artwork = V2Artwork.CHALLENGE,
                    completed = chDone,
                ) { onOpen(ContentDestination(EveluneStore.TYPE_CHALLENGE, challenge.id)) }
            }
        }
    }
}

@Composable
private fun PartnerNoteV2() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFCFB),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Surface(shape = CircleShape, color = EveluneRosePale) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text("♥", color = EveluneRoseDeep, fontSize = 22.sp)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("A note from your partner", style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
                Text("You make life brighter just by being you. ♥", style = MaterialTheme.typography.titleMedium, color = EveluneInk, maxLines = 2)
            }
            Text("Today", style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
        }
    }
}

@Composable
private fun HeroV2() {
    Box(Modifier.fillMaxWidth().height(142.dp)) {
        Column(Modifier.align(Alignment.CenterStart).padding(start = 4.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Welcome back", fontSize = 14.sp, color = EveluneRoseDeep, fontWeight = FontWeight.SemiBold)
            Text("Better together", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 40.sp, color = EveluneRoseDeep)
            Box(Modifier.width(42.dp).height(2.dp).background(EveluneRose))
            Text("Same team. Brighter days.", style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
        }
        Canvas(Modifier.align(Alignment.CenterEnd).size(112.dp)) {
            val c = EveluneRose.copy(alpha = .72f)
            drawCircle(c, radius = size.width * .23f, center = Offset(size.width * .37f, size.height * .38f), style = Stroke(width = 4.dp.toPx()))
            drawCircle(c, radius = size.width * .23f, center = Offset(size.width * .65f, size.height * .38f), style = Stroke(width = 4.dp.toPx()))
            drawLine(c, Offset(size.width * .23f, size.height * .55f), Offset(size.width * .74f, size.height * .83f), strokeWidth = 4.dp.toPx())
        }
    }
}

@Composable
private fun ActivityCardV2(
    label: String,
    icon: String,
    title: String,
    artwork: V2Artwork,
    subtitle: String? = null,
    completed: Boolean,
    onClick: (() -> Unit)?,
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
        color = EveluneCard,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 152.dp).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(start = 6.dp, top = 5.dp, bottom = 5.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = CircleShape, color = EveluneRose) {
                        Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                            Text(icon, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(label, style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep, letterSpacing = .4.sp)
                    if (completed) Text("✓", color = EveluneRoseDeep, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    title,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EveluneInk,
                )
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
            }
            Box(
                Modifier.width(116.dp).height(132.dp).clip(RoundedCornerShape(23.dp)),
            ) {
                ArtworkV2(artwork)
                if (onClick != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                        shape = CircleShape,
                        color = Color(0xFFFFFAF8),
                        shadowElevation = 2.dp,
                    ) {
                        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Text("›", color = EveluneRoseDeep, fontSize = 30.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtworkV2(type: V2Artwork) {
    Canvas(Modifier.fillMaxSize()) {
        when (type) {
            V2Artwork.QUESTION -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFFFC7B8), Color(0xFFE79495))))
                drawCircle(Color(0xFFFFE2AA), radius = size.width * .16f, center = Offset(size.width * .72f, size.height * .22f))
                val dark = Color(0xFF5A3A49)
                drawLine(dark, Offset(0f, size.height), Offset(size.width * .55f, size.height * .42f), strokeWidth = 44.dp.toPx())
                drawLine(dark, Offset(size.width * .35f, size.height), Offset(size.width * .77f, size.height * .35f), strokeWidth = 38.dp.toPx())
            }
            V2Artwork.GAME -> {
                drawRect(Color(0xFFF0C8C1))
                drawRoundRect(Color(0xFFD88E90), Offset(size.width*.17f,size.height*.24f), androidx.compose.ui.geometry.Size(size.width*.54f,size.height*.54f), androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()))
                drawRoundRect(Color(0xFFFFF0EC), Offset(size.width*.38f,size.height*.35f), androidx.compose.ui.geometry.Size(size.width*.46f,size.height*.50f), androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()))
                drawCircle(EveluneRoseDeep, radius = size.width*.08f, center = Offset(size.width*.61f,size.height*.59f))
            }
            V2Artwork.CARD -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFE5B9A7), Color(0xFFCA8F83))))
                drawRoundRect(Color(0xFFFFF7ED), Offset(size.width*.22f,size.height*.16f), androidx.compose.ui.geometry.Size(size.width*.62f,size.height*.70f), androidx.compose.ui.geometry.CornerRadius(7.dp.toPx()))
            }
            V2Artwork.CHALLENGE -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFE7B5A0), Color(0xFF9B625E))))
                drawCircle(Color(0xFFE9A08C), radius = size.width*.28f, center = Offset(size.width*.72f,size.height*.18f))
                drawRoundRect(Color(0xFFD77C70), Offset(size.width*.20f,size.height*.56f), androidx.compose.ui.geometry.Size(size.width*.28f,size.height*.23f), androidx.compose.ui.geometry.CornerRadius(11.dp.toPx()))
                drawRoundRect(Color(0xFFBE6265), Offset(size.width*.50f,size.height*.56f), androidx.compose.ui.geometry.Size(size.width*.27f,size.height*.23f), androidx.compose.ui.geometry.CornerRadius(11.dp.toPx()))
            }
        }
    }
}

@Composable
private fun HomeBackdropV2() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(Color(0xFFFAD8D4).copy(alpha = .32f), radius = size.width * .56f, center = Offset(size.width * .98f, size.height * .06f))
        drawCircle(Color(0xFFFFE4DC).copy(alpha = .46f), radius = size.width * .42f, center = Offset(size.width * .03f, size.height * .38f))
    }
}

@Composable
private fun QuestionChatDetailV2(
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
    val pair = remember(refreshKey, localRefresh, actor, questionId) { store.pair(questionId, EveluneStore.TYPE_QUESTION) }
    val mine = if (actor == LocalActor.YOU) pair.you else pair.partner
    var answer by remember(questionId, actor, refreshKey) { mutableStateOf(mine?.payload.orEmpty()) }
    var message by remember { mutableStateOf("") }
    val thread = remember(localRefresh, refreshKey, questionId) { social.messages(questionId) }
    BackHandler(onBack = onBack)

    Column(modifier.fillMaxSize().background(EveluneBackground).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EveluneRoseDeep) }
            Column(Modifier.weight(1f)) {
                Text("Daily question", style = MaterialTheme.typography.labelMedium, color = EveluneRoseDeep)
                Text(question.category, style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
            }
            Surface(shape = RoundedCornerShape(18.dp), color = EveluneRosePale) {
                Text("As ${actor.display}", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = EveluneRoseDeep)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(question.prompt, fontFamily = FontFamily.Serif, fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold, color = EveluneInk)
            }
            if (mine == null || !pair.bothSubmitted) {
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
                    ) { Text(if (mine == null) "Submit answer" else "Update answer") }
                }
            }

            val updatedPair = remember(refreshKey, localRefresh, questionId) { store.pair(questionId, EveluneStore.TYPE_QUESTION) }
            if (updatedPair.you != null || updatedPair.partner != null) {
                item { Text("Conversation", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep) }
                val initial = listOfNotNull(updatedPair.you, updatedPair.partner).sortedBy { it.submittedAt }
                items(initial, key = { "initial-${it.actor.key}" }) { submission ->
                    if (updatedPair.bothSubmitted || submission.actor == actor) {
                        ChatBubbleV2(
                            actor = submission.actor,
                            currentActor = actor,
                            partnerName = store.partnerName,
                            text = submission.payload,
                            at = submission.submittedAt,
                            initial = true,
                        )
                    }
                }
                if (!updatedPair.bothSubmitted) {
                    item {
                        Surface(color = EveluneRosePale, shape = RoundedCornerShape(20.dp)) {
                            Text(
                                "Waiting for the other answer. Your response remains private until both partners submit.",
                                Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = EveluneMuted,
                            )
                        }
                    }
                } else {
                    items(thread, key = { "message-${it.id}" }) { msg ->
                        ChatBubbleV2(msg.actor, actor, store.partnerName, msg.text, msg.sentAt, initial = false)
                    }
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
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = if (message.isNotBlank()) Color.White else EveluneMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleV2(
    actor: LocalActor,
    currentActor: LocalActor,
    partnerName: String,
    text: String,
    at: Long,
    initial: Boolean,
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
            Column(Modifier.widthIn(max = 310.dp).padding(horizontal = 15.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                if (initial) Text("Answer", style = MaterialTheme.typography.labelMedium, color = if (mine) Color.White.copy(alpha = .72f) else EveluneRoseDeep)
                Text(text, style = MaterialTheme.typography.bodyLarge, color = if (mine) Color.White else EveluneInk)
                Text(v2FormatTime(at), style = MaterialTheme.typography.labelMedium, color = if (mine) Color.White.copy(alpha = .68f) else EveluneMuted)
            }
        }
    }
}

@Composable
private fun DiscussV2(
    store: EveluneStore,
    social: EveluneSocialStore,
    refreshKey: Int,
    onOpen: (ContentDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val submissions = remember(refreshKey) { store.allSubmissions() }
    val grouped = remember(refreshKey) {
        submissions
            .filter { it.contentType == EveluneStore.TYPE_QUESTION || it.contentType == EveluneStore.TYPE_CARD }
            .groupBy { it.contentType to it.contentId }
            .entries
            .sortedByDescending { entry ->
                val latestSubmission = entry.value.maxOfOrNull { it.submittedAt } ?: 0L
                val latestChat = if (entry.key.first == EveluneStore.TYPE_QUESTION) social.latestMessage(entry.key.second)?.sentAt ?: 0L else 0L
                maxOf(latestSubmission, latestChat)
            }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Discuss", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = EveluneRoseDeep)
                Text("Each question becomes its own conversation.", style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
            }
        }
        if (grouped.isEmpty()) {
            item {
                Surface(color = EveluneCard, shape = RoundedCornerShape(24.dp)) {
                    Text("Answer a question or card and it will appear here.", Modifier.padding(18.dp), color = EveluneMuted)
                }
            }
        }
        items(grouped, key = { "${it.key.first}|${it.key.second}" }) { entry ->
            val type = entry.key.first
            val id = entry.key.second
            val pair = store.pair(id, type)
            val question = if (type == EveluneStore.TYPE_QUESTION) EveluneContentBank.questions.firstOrNull { it.id == id } else null
            val card = if (type == EveluneStore.TYPE_CARD) EveluneContentBank.cardGames.firstOrNull { it.id == id } else null
            val title = question?.prompt ?: card?.title ?: id
            val latestChat = if (question != null) social.latestMessage(id) else null
            val preview = when {
                !pair.bothSubmitted -> "Waiting for the other answer · still private"
                latestChat != null -> latestChat.text
                question != null -> "Both answers unlocked · open the chat"
                else -> "Comparison unlocked"
            }
            val stamp = latestChat?.sentAt ?: pair.latestAt ?: 0L

            Surface(
                onClick = { onOpen(ContentDestination(type, id)) },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFFCFB),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 1.dp,
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = CircleShape, color = EveluneRosePale) {
                        Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                            Icon(if (question != null) Icons.Filled.Chat else Icons.Filled.Favorite, contentDescription = null, tint = EveluneRoseDeep, modifier = Modifier.size(22.dp))
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (question != null) "QUESTION CHAT" else "CARD RESULTS", style = MaterialTheme.typography.labelMedium, color = EveluneRoseDeep)
                        Text(title, style = MaterialTheme.typography.titleMedium, color = EveluneInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(preview, style = MaterialTheme.typography.bodyMedium, color = EveluneMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (stamp > 0) Text(v2FormatTime(stamp).substringBefore(" · "), style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
                }
            }
        }
    }
}

@Composable
private fun TimelineV2(social: EveluneSocialStore, modifier: Modifier = Modifier) {
    var refresh by remember { mutableIntStateOf(0) }
    var adding by remember { mutableStateOf(false) }
    val memories = remember(refresh) { social.memories() }

    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Timeline", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = EveluneRoseDeep)
                    Text("The dates and moments you want to keep.", style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
                }
            }
            if (memories.isEmpty()) {
                item {
                    Surface(color = EveluneCard, shape = RoundedCornerShape(26.dp)) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("No memories yet", style = MaterialTheme.typography.titleLarge, color = EveluneInk)
                            Text("Tap + to add photos or videos from a date and save what made it special.", style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
                        }
                    }
                }
            }
            items(memories, key = { it.id }) { memory -> MemoryCardV2(memory) }
        }

        FloatingActionButton(
            onClick = { adding = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 22.dp),
            containerColor = EveluneRoseDeep,
            contentColor = Color.White,
            shape = CircleShape,
        ) { Icon(Icons.Filled.Add, contentDescription = "Add memory") }
    }

    if (adding) {
        AddMemoryDialogV2(
            onDismiss = { adding = false },
            onSave = { date, title, description, why, favourite, media ->
                social.addMemory(date, title, description, why, favourite, media)
                adding = false
                refresh++
            },
        )
    }
}

@Composable
private fun MemoryCardV2(memory: MemoryMoment) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFFFFCFB), shape = RoundedCornerShape(28.dp), shadowElevation = 1.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            if (memory.mediaUris.isNotEmpty()) MediaCollageV2(memory)
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(v2FormatDate(memory.dateMillis).uppercase(), style = MaterialTheme.typography.labelMedium, color = EveluneRoseDeep, letterSpacing = .5.sp)
                Text(memory.title.ifBlank { "Our date" }, fontFamily = FontFamily.Serif, fontSize = 25.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold, color = EveluneInk)
                if (memory.description.isNotBlank()) Text(memory.description, style = MaterialTheme.typography.bodyLarge, color = EveluneInk)
                if (memory.whySpecial.isNotBlank()) {
                    Text("Why it was special", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
                    Text(memory.whySpecial, style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
                }
                if (memory.favouritePart.isNotBlank()) {
                    Text("Favourite part", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
                    Text(memory.favouritePart, style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
                }
            }
        }
    }
}

@Composable
private fun MediaCollageV2(memory: MemoryMoment) {
    val ordered = remember(memory.id, memory.mediaUris) { memory.mediaUris.sortedBy { it.hashCode() xor memory.id.hashCode() } }
    val shown = ordered.take(4)
    val more = (ordered.size - 4).coerceAtLeast(0)
    val gap = 3.dp
    Box(Modifier.fillMaxWidth().height(236.dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))) {
        when (shown.size) {
            1 -> MediaTileV2(shown[0], Modifier.fillMaxSize())
            2 -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                shown.forEach { MediaTileV2(it, Modifier.weight(1f).fillMaxHeight()) }
            }
            3 -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                MediaTileV2(shown[0], Modifier.weight(1.18f).fillMaxHeight())
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(gap)) {
                    MediaTileV2(shown[1], Modifier.weight(1f).fillMaxWidth())
                    MediaTileV2(shown[2], Modifier.weight(1f).fillMaxWidth())
                }
            }
            else -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(gap)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    MediaTileV2(shown[0], Modifier.weight(1f).fillMaxHeight())
                    MediaTileV2(shown[1], Modifier.weight(1f).fillMaxHeight())
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    MediaTileV2(shown[2], Modifier.weight(1f).fillMaxHeight())
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        MediaTileV2(shown[3], Modifier.fillMaxSize())
                        if (more > 0) {
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .34f)), contentAlignment = Alignment.Center) {
                                Text("+$more", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaTileV2(uriString: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uri = remember(uriString) { uriString.toUri() }
    val mime = remember(uriString) { context.contentResolver.getType(uri).orEmpty() }
    val image by produceState<ImageBitmap?>(initialValue = null, uriString) {
        value = withContext(Dispatchers.IO) { loadThumbV2(context, uri, mime) }
    }
    Box(modifier.background(EveluneRosePale), contentAlignment = Alignment.Center) {
        if (image != null) {
            Image(image!!, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(Icons.Filled.Image, contentDescription = null, tint = EveluneMuted, modifier = Modifier.size(28.dp))
        }
        if (mime.startsWith("video/")) {
            Surface(shape = CircleShape, color = Color.Black.copy(alpha = .44f)) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Video", tint = Color.White)
                }
            }
        }
    }
}

private fun loadThumbV2(context: android.content.Context, uri: Uri, mime: String): ImageBitmap? = try {
    val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= 29) {
        context.contentResolver.loadThumbnail(uri, Size(700, 700), null)
    } else if (mime.startsWith("video/")) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            retriever.getFrameAtTime(0)
        } finally {
            retriever.release()
        }
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
    bitmap?.asImageBitmap()
} catch (_: Exception) {
    null
}

@Composable
private fun AddMemoryDialogV2(
    onDismiss: () -> Unit,
    onSave: (Long, String, String, String, String, List<String>) -> Unit,
) {
    val context = LocalContext.current
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var whySpecial by remember { mutableStateOf("") }
    var favourite by remember { mutableStateOf("") }
    val media = remember { mutableStateListOf<String>() }
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
            }
            if (uri.toString() !in media) media += uri.toString()
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = EveluneBackground) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.ArrowBack, contentDescription = "Close", tint = EveluneRoseDeep) }
                    Text("Add a memory", fontFamily = FontFamily.Serif, fontSize = 27.sp, fontWeight = FontWeight.Bold, color = EveluneInk)
                }
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    Text("Date", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
                    Surface(
                        onClick = {
                            val local = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    dateMillis = LocalDate.of(year, month + 1, day)
                                        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                },
                                local.year,
                                local.monthValue - 1,
                                local.dayOfMonth,
                            ).show()
                        },
                        color = Color(0xFFFFFCFB),
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 1.dp,
                    ) {
                        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = EveluneRoseDeep)
                            Spacer(Modifier.width(10.dp))
                            Text(v2FormatDate(dateMillis), style = MaterialTheme.typography.bodyLarge, color = EveluneInk)
                        }
                    }

                    Button(
                        onClick = { picker.launch(arrayOf("image/*", "video/*")) },
                        colors = ButtonDefaults.buttonColors(containerColor = EveluneRoseDeep),
                        shape = RoundedCornerShape(19.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(7.dp))
                        Text("Add photos or videos")
                    }
                    if (media.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(media, key = { it }) { uri ->
                                Box(Modifier.size(82.dp).clip(RoundedCornerShape(17.dp))) {
                                    MediaTileV2(uri, Modifier.fillMaxSize())
                                }
                            }
                        }
                    }

                    OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(20.dp), label = { Text("Title") }, placeholder = { Text("Dinner by the river") })
                    OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(20.dp), label = { Text("What did you do?") })
                    OutlinedTextField(whySpecial, { whySpecial = it }, Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(20.dp), label = { Text("Why was it special?") })
                    OutlinedTextField(favourite, { favourite = it }, Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(20.dp), label = { Text("Favourite part") })
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = { onSave(dateMillis, title, description, whySpecial, favourite, media.toList()) },
                    enabled = description.isNotBlank() || media.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EveluneRoseDeep),
                    shape = RoundedCornerShape(21.dp),
                ) { Text("Save memory") }
            }
        }
    }
}

@Composable
private fun BottomBarV2(selected: Int, labels: List<String>, onSelect: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFCFA),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 9.dp,
    ) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            labels.forEachIndexed { index, label ->
                val tint = if (selected == index) EveluneRoseDeep else EveluneMuted
                val icon = when (index) {
                    0 -> Icons.Filled.Home
                    1 -> Icons.Filled.Search
                    2 -> Icons.Filled.Chat
                    3 -> Icons.Filled.DateRange
                    else -> Icons.Filled.Favorite
                }
                Surface(onClick = { onSelect(index) }, color = Color.Transparent, shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(23.dp))
                        Text(label, fontSize = 11.sp, color = tint, fontWeight = if (selected == index) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}
