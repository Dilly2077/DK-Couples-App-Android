package com.dk.together.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.content.DailyCardGame
import com.dk.together.content.DailyChallenge
import com.dk.together.content.DailyQuestion
import com.dk.together.content.EveluneContentBank
import com.dk.together.data.EveluneStore
import com.dk.together.data.LocalActor
import com.dk.together.data.Submission
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRose
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class ContentDestination(val type: String, val id: String)

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale.getDefault())
private fun formatTime(epoch: Long): String = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).format(dateFormatter)
private fun key(type: String, id: String, actor: LocalActor) = "$type|$id|${actor.key}"

@Composable
internal fun ExploreUi(
    store: EveluneStore,
    actor: LocalActor,
    refreshKey: Int,
    onOpen: (ContentDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    val submissions = remember(refreshKey, actor) { store.allSubmissions().associateBy { key(it.contentType, it.contentId, it.actor) } }
    val tabs = listOf("Questions", "Cards", "Challenges")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeading("Explore", "Search the full two-year library.")
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = EveluneMuted) },
                placeholder = { Text("Search by topic or phrase") },
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index, label ->
                    FilterChip(selected = selected == index, onClick = { selected = index }, label = { Text(label) })
                }
            }
        }

        when (selected) {
            0 -> {
                val list = EveluneContentBank.questions.filter {
                    query.isBlank() || it.prompt.contains(query, true) || it.category.contains(query, true)
                }
                items(list, key = { it.id }) { item ->
                    val mine = submissions[key(EveluneStore.TYPE_QUESTION, item.id, actor)]
                    val other = submissions[key(EveluneStore.TYPE_QUESTION, item.id, actor.other())]
                    BankRow(item.category, item.prompt, mine, mine != null && other != null) {
                        onOpen(ContentDestination(EveluneStore.TYPE_QUESTION, item.id))
                    }
                }
            }
            1 -> {
                val list = EveluneContentBank.cardGames.filter { card ->
                    query.isBlank() || card.title.contains(query, true) || card.category.contains(query, true) || card.items.any { it.prompt.contains(query, true) }
                }
                items(list, key = { it.id }) { item ->
                    val mine = submissions[key(EveluneStore.TYPE_CARD, item.id, actor)]
                    val other = submissions[key(EveluneStore.TYPE_CARD, item.id, actor.other())]
                    BankRow(item.category, "${item.title} · 4 rated prompts", mine, mine != null && other != null) {
                        onOpen(ContentDestination(EveluneStore.TYPE_CARD, item.id))
                    }
                }
            }
            else -> {
                val list = EveluneContentBank.challenges.filter {
                    query.isBlank() || it.prompt.contains(query, true) || it.category.contains(query, true)
                }
                items(list, key = { it.id }) { item ->
                    val mine = submissions[key(EveluneStore.TYPE_CHALLENGE, item.id, actor)]
                    val other = submissions[key(EveluneStore.TYPE_CHALLENGE, item.id, actor.other())]
                    BankRow(item.category, EveluneContentBank.render(item.prompt, store.partnerName), mine, mine != null && other != null) {
                        onOpen(ContentDestination(EveluneStore.TYPE_CHALLENGE, item.id))
                    }
                }
            }
        }
    }
}

@Composable
private fun BankRow(category: String, title: String, mine: Submission?, both: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFCFB),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(category.uppercase(), style = MaterialTheme.typography.labelMedium, color = EveluneRoseDeep, letterSpacing = .5.sp)
                Text(title, style = MaterialTheme.typography.titleMedium, color = EveluneInk, maxLines = 3, overflow = TextOverflow.Ellipsis)
                if (mine != null) {
                    Text(
                        if (both) "Both complete · ${formatTime(maxOf(mine.submittedAt, mine.submittedAt))}" else "Completed · ${formatTime(mine.submittedAt)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = EveluneMuted,
                    )
                }
            }
            if (mine != null) Icon(Icons.Filled.CheckCircle, contentDescription = "Completed", tint = EveluneRoseDeep, modifier = Modifier.size(25.dp))
        }
    }
}

@Composable
internal fun ContentDetailUi(
    destination: ContentDestination,
    store: EveluneStore,
    actor: LocalActor,
    refreshKey: Int,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val question = if (destination.type == EveluneStore.TYPE_QUESTION) EveluneContentBank.questions.firstOrNull { it.id == destination.id } else null
    val card = if (destination.type == EveluneStore.TYPE_CARD) EveluneContentBank.cardGames.firstOrNull { it.id == destination.id } else null
    val challenge = if (destination.type == EveluneStore.TYPE_CHALLENGE) EveluneContentBank.challenges.firstOrNull { it.id == destination.id } else null

    Scaffold(containerColor = EveluneBackground) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EveluneRoseDeep) }
                    Text("Back", color = EveluneRoseDeep, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(18.dp), color = EveluneRosePale) {
                        Text("Answering as ${actor.display}", modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), style = MaterialTheme.typography.labelMedium, color = EveluneRoseDeep)
                    }
                }
            }
            question?.let { item { QuestionDetail(it, store, actor, refreshKey, onSaved) } }
            card?.let { item { CardDetail(it, store, actor, refreshKey, onSaved) } }
            challenge?.let { item { ChallengeDetail(it, store, actor, refreshKey, onSaved) } }
        }
    }
}

@Composable
private fun QuestionDetail(question: DailyQuestion, store: EveluneStore, actor: LocalActor, refreshKey: Int, onSaved: () -> Unit) {
    val pair = remember(refreshKey, actor, question.id) { store.pair(question.id, EveluneStore.TYPE_QUESTION) }
    val mine = if (actor == LocalActor.YOU) pair.you else pair.partner
    var answer by remember(question.id, actor, refreshKey) { mutableStateOf(mine?.payload.orEmpty()) }

    DetailHeader("DAILY QUESTION", question.category, question.prompt)
    OutlinedTextField(
        value = answer,
        onValueChange = { answer = it },
        modifier = Modifier.fillMaxWidth(),
        minLines = 5,
        shape = RoundedCornerShape(24.dp),
        placeholder = { Text("Write your answer") },
    )
    Button(
        onClick = {
            store.save(question.id, EveluneStore.TYPE_QUESTION, actor, answer.trim())
            onSaved()
        },
        enabled = answer.isNotBlank(),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EveluneRoseDeep),
        modifier = Modifier.fillMaxWidth().height(54.dp),
    ) { Text(if (mine == null) "Submit answer" else "Update answer") }

    val updated = remember(refreshKey, question.id) { store.pair(question.id, EveluneStore.TYPE_QUESTION) }
    RevealQuestion(updated, question)
}

@Composable
private fun RevealQuestion(pair: com.dk.together.data.PairSubmission, question: DailyQuestion) {
    if (pair.bothSubmitted) {
        SectionLabel("SHARED ANSWERS")
        AnswerSurface("You", pair.you!!.payload, pair.you.submittedAt)
        AnswerSurface("Partner", pair.partner!!.payload, pair.partner.submittedAt)
    } else {
        val single = pair.you ?: pair.partner
        if (single != null) WaitingSurface("Saved ${formatTime(single.submittedAt)}. Your answer stays private until both partners submit.")
        else WaitingSurface("Both answers stay private until both partners submit.")
    }
}

@Composable
private fun CardDetail(card: DailyCardGame, store: EveluneStore, actor: LocalActor, refreshKey: Int, onSaved: () -> Unit) {
    val pair = remember(refreshKey, actor, card.id) { store.pair(card.id, EveluneStore.TYPE_CARD) }
    val mine = if (actor == LocalActor.YOU) pair.you else pair.partner
    val existing = mine?.payload?.split(',')?.mapNotNull { it.toIntOrNull() }.orEmpty()
    val ratings = remember(card.id, actor, refreshKey) { mutableStateListOf<Int>().apply { repeat(4) { add(existing.getOrElse(it) { 0 }) } } }

    DetailHeader("DAILY CARD", card.category, card.title)
    Text("Rate every statement from 1–4. Neither set of ratings is revealed until both partners submit.", style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
    Surface(color = EveluneRosePale, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("1  Not at all true / very unlikely", style = MaterialTheme.typography.bodyMedium, color = EveluneInk)
            Text("4  Very true / very likely", style = MaterialTheme.typography.bodyMedium, color = EveluneInk)
        }
    }

    card.items.forEachIndexed { index, item ->
        Surface(color = Color(0xFFFFFCFB), shape = RoundedCornerShape(24.dp), shadowElevation = 1.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(EveluneContentBank.render(item.prompt, store.partnerName), style = MaterialTheme.typography.titleMedium, color = EveluneInk)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..4).forEach { rating ->
                        FilterChip(
                            selected = ratings[index] == rating,
                            onClick = { ratings[index] = rating },
                            label = { Text(rating.toString()) },
                        )
                    }
                }
            }
        }
    }
    Button(
        onClick = {
            store.save(card.id, EveluneStore.TYPE_CARD, actor, ratings.joinToString(","))
            onSaved()
        },
        enabled = ratings.all { it in 1..4 },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EveluneRoseDeep),
        modifier = Modifier.fillMaxWidth().height(54.dp),
    ) { Text(if (mine == null) "Submit ratings" else "Update ratings") }

    val updated = remember(refreshKey, card.id) { store.pair(card.id, EveluneStore.TYPE_CARD) }
    RevealCard(updated, card)
}

@Composable
private fun RevealCard(pair: com.dk.together.data.PairSubmission, card: DailyCardGame) {
    if (!pair.bothSubmitted) {
        val single = pair.you ?: pair.partner
        if (single != null) WaitingSurface("Ratings saved ${formatTime(single.submittedAt)}. The comparison unlocks after both partners submit.")
        else WaitingSurface("The comparison unlocks after both partners submit.")
        return
    }
    val first = pair.you!!.payload.split(',').mapNotNull { it.toIntOrNull() }
    val second = pair.partner!!.payload.split(',').mapNotNull { it.toIntOrNull() }
    if (first.size != 4 || second.size != 4) return

    SectionLabel("SHARED RESULTS")
    card.items.forEachIndexed { index, item ->
        Surface(color = Color(0xFFFFFCFB), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.prompt.replace(EveluneContentBank.PARTNER_TOKEN, "your partner"), style = MaterialTheme.typography.bodyLarge, color = EveluneInk)
                Text("You ${first[index]}   ·   Partner ${second[index]}   ·   Mean ${"%.1f".format((first[index] + second[index]) / 2.0)}", style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
            }
        }
    }
    val mean = EveluneContentBank.scoreMean(first, second)
    val difference = EveluneContentBank.averageDifference(first, second)
    Surface(color = EveluneRosePale, shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Card average  ${"%.2f".format(mean)} / 4", style = MaterialTheme.typography.titleMedium, color = EveluneInk)
            Text("Average rating difference  ${"%.2f".format(difference)}", style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
            Text("The average and the difference are shown separately: a high average does not automatically mean close agreement.", style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
        }
    }
}

@Composable
private fun ChallengeDetail(challenge: DailyChallenge, store: EveluneStore, actor: LocalActor, refreshKey: Int, onSaved: () -> Unit) {
    val pair = remember(refreshKey, actor, challenge.id) { store.pair(challenge.id, EveluneStore.TYPE_CHALLENGE) }
    val mine = if (actor == LocalActor.YOU) pair.you else pair.partner
    DetailHeader("DAILY CHALLENGE", challenge.category, EveluneContentBank.render(challenge.prompt, store.partnerName))
    Text("Challenges can be completed independently. The shared state shows when both of you have done it.", style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
    Button(
        onClick = {
            store.save(challenge.id, EveluneStore.TYPE_CHALLENGE, actor, "done")
            onSaved()
        },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (mine == null) EveluneRoseDeep else EveluneRose),
        modifier = Modifier.fillMaxWidth().height(54.dp),
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(19.dp))
        Spacer(Modifier.size(8.dp))
        Text(if (mine == null) "Mark complete" else "Completed ${formatTime(mine.submittedAt)}")
    }
    val updated = remember(refreshKey, challenge.id) { store.pair(challenge.id, EveluneStore.TYPE_CHALLENGE) }
    if (updated.bothSubmitted) {
        Surface(color = EveluneRosePale, shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Both completed ✓", style = MaterialTheme.typography.titleMedium, color = EveluneRoseDeep)
                Text("You · ${formatTime(updated.you!!.submittedAt)}", style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
                Text("Partner · ${formatTime(updated.partner!!.submittedAt)}", style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
            }
        }
    }
}

@Composable
internal fun DiscussUi(store: EveluneStore, refreshKey: Int, onOpen: (ContentDestination) -> Unit, modifier: Modifier = Modifier) {
    val submissions = remember(refreshKey) { store.allSubmissions() }
    val grouped = submissions
        .filter { it.contentType != EveluneStore.TYPE_CHALLENGE }
        .groupBy { it.contentType to it.contentId }
        .entries
        .sortedByDescending { entry -> entry.value.maxOfOrNull { it.submittedAt } ?: 0L }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeading("Discuss", "Your saved answers and unlocked comparisons.") }
        if (grouped.isEmpty()) {
            item { EmptySurface("Nothing here yet. Answer a question or card and it will appear here.") }
        }
        items(grouped, key = { "${it.key.first}|${it.key.second}" }) { entry ->
            val type = entry.key.first
            val id = entry.key.second
            val pair = store.pair(id, type)
            val title = contentTitle(type, id, store.partnerName)
            Surface(
                onClick = { onOpen(ContentDestination(type, id)) },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFFCFB),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 1.dp,
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(if (type == EveluneStore.TYPE_QUESTION) "QUESTION" else "CARD", style = MaterialTheme.typography.labelMedium, color = EveluneRoseDeep)
                    Text(title, style = MaterialTheme.typography.titleMedium, color = EveluneInk, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    if (pair.bothSubmitted) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EveluneRoseDeep, modifier = Modifier.size(18.dp))
                            Text("Unlocked · ${formatTime(pair.latestAt!!)}", style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
                        }
                        if (type == EveluneStore.TYPE_QUESTION) {
                            Text("You: ${pair.you!!.payload}", style = MaterialTheme.typography.bodyMedium, color = EveluneInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("Partner: ${pair.partner!!.payload}", style = MaterialTheme.typography.bodyMedium, color = EveluneInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    } else {
                        Text("Waiting for the other answer · still private", style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
                    }
                }
            }
        }
    }
}

@Composable
internal fun TimelineUi(store: EveluneStore, refreshKey: Int, modifier: Modifier = Modifier) {
    val submissions = remember(refreshKey) { store.allSubmissions() }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { ScreenHeading("Timeline", "A local log of answers, ratings and completed challenges.") }
        if (submissions.isEmpty()) item { EmptySurface("Your activity history will appear here.") }
        items(submissions, key = { "${it.contentType}|${it.contentId}|${it.actor.key}" }) { row ->
            Surface(color = Color(0xFFFFFCFB), shape = RoundedCornerShape(22.dp)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EveluneRoseDeep, modifier = Modifier.size(23.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(contentTitle(row.contentType, row.contentId, store.partnerName), style = MaterialTheme.typography.bodyLarge, color = EveluneInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("${row.actor.display} · ${typeLabel(row.contentType)} · ${formatTime(row.submittedAt)}", style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
                    }
                }
            }
        }
    }
}

@Composable
internal fun UsUi(store: EveluneStore, actor: LocalActor, onActorChanged: (LocalActor) -> Unit, modifier: Modifier = Modifier) {
    var partnerName by remember { mutableStateOf(store.partnerName) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { ScreenHeading("Us", "Local preview controls while pairing is still to come.") }
        item {
            Surface(color = Color(0xFFFFFCFB), shape = RoundedCornerShape(26.dp), shadowElevation = 1.dp) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Answering as", style = MaterialTheme.typography.titleMedium, color = EveluneInk)
                    Text("This switch exists so the two-sided answer/reveal flow can be tested on one phone. Real account pairing will replace it later.", style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LocalActor.entries.forEach { option ->
                            FilterChip(selected = actor == option, onClick = { onActorChanged(option) }, label = { Text(option.display) })
                        }
                    }
                }
            }
        }
        item {
            Surface(color = Color(0xFFFFFCFB), shape = RoundedCornerShape(26.dp), shadowElevation = 1.dp) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Partner name", style = MaterialTheme.typography.titleMedium, color = EveluneInk)
                    OutlinedTextField(
                        value = partnerName,
                        onValueChange = { partnerName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        placeholder = { Text("Partner") },
                    )
                    Button(
                        onClick = { store.partnerName = partnerName },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EveluneRoseDeep),
                    ) { Text("Save name") }
                }
            }
        }
    }
}

@Composable
private fun ScreenHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, color = EveluneRoseDeep)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
    }
}

@Composable
private fun DetailHeader(label: String, category: String, title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep, letterSpacing = .7.sp)
        Text(category, style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
        Text(title, fontFamily = FontFamily.Serif, fontSize = 31.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold, color = EveluneInk)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep, letterSpacing = .7.sp)
}

@Composable
private fun AnswerSurface(label: String, answer: String, at: Long) {
    Surface(color = Color(0xFFFFFCFB), shape = RoundedCornerShape(24.dp), shadowElevation = 1.dp) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep)
            Text(answer, style = MaterialTheme.typography.bodyLarge, color = EveluneInk)
            Text(formatTime(at), style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
        }
    }
}

@Composable
private fun WaitingSurface(text: String) {
    Surface(color = EveluneRosePale, shape = RoundedCornerShape(22.dp)) {
        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
    }
}

@Composable
private fun EmptySurface(text: String) {
    Surface(color = EveluneCard, shape = RoundedCornerShape(26.dp)) {
        Text(text, modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
    }
}

private fun typeLabel(type: String) = when (type) {
    EveluneStore.TYPE_QUESTION -> "Question"
    EveluneStore.TYPE_CARD -> "Card"
    EveluneStore.TYPE_CHALLENGE -> "Challenge"
    else -> type
}

private fun contentTitle(type: String, id: String, partnerName: String): String = when (type) {
    EveluneStore.TYPE_QUESTION -> EveluneContentBank.questions.firstOrNull { it.id == id }?.prompt ?: id
    EveluneStore.TYPE_CARD -> EveluneContentBank.cardGames.firstOrNull { it.id == id }?.title ?: id
    EveluneStore.TYPE_CHALLENGE -> EveluneContentBank.challenges.firstOrNull { it.id == id }?.let { EveluneContentBank.render(it.prompt, partnerName) } ?: id
    else -> id
}
