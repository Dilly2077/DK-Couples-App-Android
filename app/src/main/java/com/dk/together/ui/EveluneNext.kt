package com.dk.together.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.content.EveluneContentBank
import com.dk.together.data.EveluneSocialStore
import com.dk.together.data.EveluneStore
import com.dk.together.data.LocalActor
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRose
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale

private enum class HomeArtwork { QUESTION, GAME, CARD, CHALLENGE }

@Composable
fun EveluneNextApp() {
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
                EveluneNavBar(selected = selected, labels = labels, onSelect = { selected = it })
            }
        },
    ) { innerPadding ->
        val open = destination
        if (open != null) {
            if (open.type == EveluneStore.TYPE_QUESTION) {
                QuestionChatScreen(
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
                0 -> EveluneHome(
                    store = store,
                    actor = actor,
                    refreshKey = refreshKey,
                    onOpen = { destination = it },
                    modifier = Modifier.padding(innerPadding),
                )
                1 -> ExploreUi(
                    store,
                    actor,
                    refreshKey,
                    { destination = it },
                    Modifier.padding(innerPadding).statusBarsPadding(),
                )
                2 -> DiscussChatList(
                    store = store,
                    social = social,
                    refreshKey = refreshKey,
                    onOpen = { destination = it },
                    modifier = Modifier.padding(innerPadding).statusBarsPadding(),
                )
                3 -> MemoryTimelineScreen(
                    social = social,
                    modifier = Modifier.padding(innerPadding).statusBarsPadding(),
                )
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
private fun EveluneHome(
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
    val cardDone = remember(refreshKey, actor, card.id) { store.completedBy(card.id, EveluneStore.TYPE_CARD, actor) != null }
    val challengeDone = remember(refreshKey, actor, challenge.id) { store.completedBy(challenge.id, EveluneStore.TYPE_CHALLENGE, actor) != null }

    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        HomeBackdrop()
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            item { PartnerNoteStrip() }
            item { HomeHero() }
            item {
                HomeActivityCard(
                    label = "DAILY QUESTION",
                    symbol = "?",
                    title = question.prompt,
                    artwork = HomeArtwork.QUESTION,
                    completed = qDone,
                    onClick = { onOpen(ContentDestination(EveluneStore.TYPE_QUESTION, question.id)) },
                )
            }
            item {
                HomeActivityCard(
                    label = "DAILY GAME",
                    symbol = "✦",
                    title = "Pixel games",
                    subtitle = "Coming in the next stage.",
                    artwork = HomeArtwork.GAME,
                    completed = false,
                    onClick = null,
                )
            }
            item {
                HomeActivityCard(
                    label = "DAILY CARD",
                    symbol = "□",
                    title = card.title,
                    subtitle = "4 prompts · rate 1–4",
                    artwork = HomeArtwork.CARD,
                    completed = cardDone,
                    onClick = { onOpen(ContentDestination(EveluneStore.TYPE_CARD, card.id)) },
                )
            }
            item {
                HomeActivityCard(
                    label = "DAILY CHALLENGE",
                    symbol = "◎",
                    title = EveluneContentBank.render(challenge.prompt, store.partnerName),
                    artwork = HomeArtwork.CHALLENGE,
                    completed = challengeDone,
                    onClick = { onOpen(ContentDestination(EveluneStore.TYPE_CHALLENGE, challenge.id)) },
                )
            }
        }
    }
}

@Composable
private fun PartnerNoteStrip() {
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
                Text("You make life brighter just by being you. ♥", style = MaterialTheme.typography.titleMedium, color = EveluneInk)
            }
            Text("Today", style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
        }
    }
}

@Composable
private fun HomeHero() {
    Box(Modifier.fillMaxWidth().height(142.dp)) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text("Welcome back", fontSize = 14.sp, color = EveluneRoseDeep, fontWeight = FontWeight.SemiBold)
            Text(
                "Better together",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = 40.sp,
                color = EveluneRoseDeep,
            )
            Box(Modifier.width(42.dp).height(2.dp).background(EveluneRose))
            Text("Same team. Brighter days.", style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
        }
        Canvas(Modifier.align(Alignment.CenterEnd).size(112.dp)) {
            val color = EveluneRose.copy(alpha = .72f)
            val path = Path().apply {
                moveTo(size.width * .12f, size.height * .58f)
                cubicTo(size.width * .30f, size.height * .44f, size.width * .45f, size.height * .23f, size.width * .55f, size.height * .20f)
                cubicTo(size.width * .76f, size.height * .15f, size.width * .83f, size.height * .37f, size.width * .67f, size.height * .49f)
                cubicTo(size.width * .54f, size.height * .59f, size.width * .38f, size.height * .50f, size.width * .43f, size.height * .35f)
                cubicTo(size.width * .49f, size.height * .17f, size.width * .25f, size.height * .12f, size.width * .21f, size.height * .31f)
                cubicTo(size.width * .17f, size.height * .50f, size.width * .46f, size.height * .66f, size.width * .77f, size.height * .83f)
            }
            drawPath(path, color = color, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun HomeActivityCard(
    label: String,
    symbol: String,
    title: String,
    artwork: HomeArtwork,
    completed: Boolean,
    subtitle: String? = null,
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
                            Text(symbol, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(label, style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep, letterSpacing = .4.sp)
                    if (completed) Text("✓", color = EveluneRoseDeep, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                Text(title, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, color = EveluneInk)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
            }
            Box(Modifier.width(116.dp).height(132.dp).clip(RoundedCornerShape(23.dp))) {
                HomeArtworkGraphic(artwork)
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
private fun HomeArtworkGraphic(type: HomeArtwork) {
    Canvas(Modifier.fillMaxSize()) {
        when (type) {
            HomeArtwork.QUESTION -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFFFC7B8), Color(0xFFE79495))))
                drawCircle(Color(0xFFFFE2AA), radius = size.width * .16f, center = Offset(size.width * .72f, size.height * .22f))
                val mountain = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width * .42f, size.height * .48f)
                    lineTo(size.width * .62f, size.height * .72f)
                    lineTo(size.width * .78f, size.height * .38f)
                    lineTo(size.width, size.height * .66f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(mountain, Color(0xFF5B3D4B))
            }
            HomeArtwork.GAME -> {
                drawRect(Color(0xFFF0C8C1))
                drawRoundRect(Color(0xFFD88E90), Offset(size.width * .16f, size.height * .24f), Size(size.width * .56f, size.height * .56f), CornerRadius(16.dp.toPx()))
                drawRoundRect(Color(0xFFFFF0EC), Offset(size.width * .38f, size.height * .35f), Size(size.width * .46f, size.height * .50f), CornerRadius(16.dp.toPx()))
                drawCircle(EveluneRoseDeep, radius = size.width * .08f, center = Offset(size.width * .61f, size.height * .59f))
            }
            HomeArtwork.CARD -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFE5B9A7), Color(0xFFCA8F83))))
                drawRoundRect(Color(0xFFFFF7ED), Offset(size.width * .20f, size.height * .14f), Size(size.width * .64f, size.height * .72f), CornerRadius(8.dp.toPx()))
                drawLine(EveluneRoseDeep, Offset(size.width * .33f, size.height * .44f), Offset(size.width * .72f, size.height * .44f), strokeWidth = 2.dp.toPx())
                drawLine(EveluneRoseDeep, Offset(size.width * .33f, size.height * .55f), Offset(size.width * .65f, size.height * .55f), strokeWidth = 2.dp.toPx())
            }
            HomeArtwork.CHALLENGE -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFE7B5A0), Color(0xFF9B625E))))
                drawCircle(Color(0xFFE9A08C), radius = size.width * .28f, center = Offset(size.width * .72f, size.height * .18f))
                drawRoundRect(Color(0xFFD77C70), Offset(size.width * .19f, size.height * .56f), Size(size.width * .29f, size.height * .24f), CornerRadius(11.dp.toPx()))
                drawRoundRect(Color(0xFFBE6265), Offset(size.width * .50f, size.height * .56f), Size(size.width * .28f, size.height * .24f), CornerRadius(11.dp.toPx()))
            }
        }
    }
}

@Composable
private fun HomeBackdrop() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(Color(0xFFFAD8D4).copy(alpha = .32f), radius = size.width * .56f, center = Offset(size.width * .98f, size.height * .06f))
        drawCircle(Color(0xFFFFE4DC).copy(alpha = .46f), radius = size.width * .42f, center = Offset(size.width * .03f, size.height * .38f))
    }
}

@Composable
private fun EveluneNavBar(selected: Int, labels: List<String>, onSelect: (Int) -> Unit) {
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
                    Column(
                        Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(23.dp))
                        Text(label, fontSize = 11.sp, color = tint, fontWeight = if (selected == index) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}
