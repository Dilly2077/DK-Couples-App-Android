package com.dk.together.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.content.EveluneContentBank
import com.dk.together.data.EveluneStore
import com.dk.together.data.LocalActor
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRose
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale

private enum class Artwork { MOUNTAINS, CARDS, NOTE, MUGS }

@Composable
fun EveluneApp() {
    val context = LocalContext.current
    val store = remember { EveluneStore(context) }
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
                EveluneBottomBar(
                    selected = selected,
                    labels = labels,
                    onSelect = { selected = it }
                )
            }
        }
    ) { innerPadding ->
        val open = destination
        if (open != null) {
            Box(Modifier.padding(innerPadding)) {
                ContentDetailUi(
                    destination = open,
                    store = store,
                    actor = actor,
                    refreshKey = refreshKey,
                    onSaved = { refreshKey++ },
                    onBack = { destination = null },
                )
            }
        } else {
            when (selected) {
                0 -> HomeUi(store, actor, refreshKey, { destination = it }, Modifier.padding(innerPadding))
                1 -> ExploreUi(store, actor, refreshKey, { destination = it }, Modifier.padding(innerPadding).statusBarsPadding())
                2 -> DiscussUi(store, refreshKey, { destination = it }, Modifier.padding(innerPadding).statusBarsPadding())
                3 -> TimelineUi(store, refreshKey, Modifier.padding(innerPadding).statusBarsPadding())
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
private fun HomeUi(
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

    Box(modifier = modifier.fillMaxSize().background(EveluneBackground)) {
        SoftBackdrop()
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { PartnerNote() }
            item { HeroSection() }
            item {
                ActivityCard(
                    label = "DAILY QUESTION",
                    icon = "?",
                    title = question.prompt,
                    artwork = Artwork.MOUNTAINS,
                    completed = qDone,
                    onClick = { onOpen(ContentDestination(EveluneStore.TYPE_QUESTION, question.id)) },
                )
            }
            item {
                ActivityCard(
                    label = "DAILY GAME",
                    icon = "✦",
                    title = "Pixel games",
                    subtitle = "Coming in the next stage.",
                    artwork = Artwork.CARDS,
                    completed = false,
                    onClick = null,
                )
            }
            item {
                ActivityCard(
                    label = "DAILY CARD",
                    icon = "▣",
                    title = card.title,
                    subtitle = "4 prompts · rate 1–4",
                    artwork = Artwork.NOTE,
                    completed = cDone,
                    onClick = { onOpen(ContentDestination(EveluneStore.TYPE_CARD, card.id)) },
                )
            }
            item {
                ActivityCard(
                    label = "DAILY CHALLENGE",
                    icon = "◎",
                    title = EveluneContentBank.render(challenge.prompt, store.partnerName),
                    artwork = Artwork.MUGS,
                    completed = chDone,
                    onClick = { onOpen(ContentDestination(EveluneStore.TYPE_CHALLENGE, challenge.id)) },
                )
            }
        }
    }
}

@Composable
private fun PartnerNote() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFCFB),
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CoupleAvatar()
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("A note from your partner", style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
                Text(
                    "You make life brighter just by being you. ♥",
                    style = MaterialTheme.typography.titleMedium,
                    color = EveluneInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text("Today", style = MaterialTheme.typography.labelMedium, color = EveluneMuted)
        }
    }
}

@Composable
private fun CoupleAvatar() {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFFF7D0C8), Color(0xFFE5A7A8)))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(44.dp)) {
            val burgundy = Color(0xFF8E3344)
            val rose = Color(0xFFD97B82)
            drawCircle(burgundy, radius = size.minDimension * .11f, center = Offset(size.width * .35f, size.height * .28f))
            drawCircle(rose, radius = size.minDimension * .11f, center = Offset(size.width * .65f, size.height * .28f))
            drawArc(burgundy, 205f, 130f, false, Offset(size.width * .14f, size.height * .28f), Size(size.width * .52f, size.height * .52f), style = Stroke(width = size.minDimension * .10f, cap = StrokeCap.Round))
            drawArc(rose, 205f, 130f, false, Offset(size.width * .34f, size.height * .28f), Size(size.width * .52f, size.height * .52f), style = Stroke(width = size.minDimension * .10f, cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun HeroSection() {
    Box(modifier = Modifier.fillMaxWidth().height(184.dp)) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text("Welcome back", fontSize = 15.sp, color = EveluneRoseDeep, fontWeight = FontWeight.SemiBold)
            Text("Better together", fontSize = 41.sp, lineHeight = 44.sp, color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.padding(top = 4.dp).size(width = 42.dp, height = 2.dp).background(EveluneRose))
            Text("Same team. Brighter days.", modifier = Modifier.padding(top = 5.dp), style = MaterialTheme.typography.bodyLarge, color = EveluneMuted)
        }
        HeartLineArt(modifier = Modifier.align(Alignment.CenterEnd).size(135.dp))
    }
}

@Composable
private fun HeartLineArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val p = Path().apply {
            moveTo(size.width * .15f, size.height * .67f)
            cubicTo(size.width * .34f, size.height * .52f, size.width * .52f, size.height * .40f, size.width * .58f, size.height * .22f)
            cubicTo(size.width * .62f, size.height * .10f, size.width * .80f, size.height * .14f, size.width * .75f, size.height * .31f)
            cubicTo(size.width * .70f, size.height * .47f, size.width * .49f, size.height * .55f, size.width * .50f, size.height * .35f)
            cubicTo(size.width * .51f, size.height * .19f, size.width * .33f, size.height * .12f, size.width * .28f, size.height * .28f)
            cubicTo(size.width * .24f, size.height * .43f, size.width * .42f, size.height * .58f, size.width * .68f, size.height * .71f)
        }
        drawPath(p, EveluneRose.copy(alpha = .8f), style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun ActivityCard(
    label: String,
    icon: String,
    title: String,
    artwork: Artwork,
    subtitle: String? = null,
    completed: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
        color = EveluneCard,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 1.dp,
        tonalElevation = 0.dp
    ) {
        Row(modifier = Modifier.height(154.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f).padding(start = 18.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Surface(shape = CircleShape, color = EveluneRose) {
                        Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                            Text(icon, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    }
                    Text(label, style = MaterialTheme.typography.labelLarge, color = EveluneRoseDeep, letterSpacing = .5.sp)
                    if (completed) Icon(Icons.Filled.CheckCircle, contentDescription = "Completed", tint = EveluneRoseDeep, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(1.dp))
                Text(title, fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium, color = EveluneInk, maxLines = 3, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = EveluneMuted)
            }
            Box(
                modifier = Modifier.padding(end = 8.dp, top = 8.dp, bottom = 8.dp).fillMaxWidth(.38f).height(138.dp).clip(RoundedCornerShape(24.dp))
            ) {
                CardArtwork(artwork)
                if (onClick != null) {
                    Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(11.dp), shape = CircleShape, color = Color(0xFFFFF8F6).copy(alpha = .95f), shadowElevation = 2.dp) {
                        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            Text("›", fontSize = 34.sp, color = EveluneRoseDeep, fontWeight = FontWeight.Light)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardArtwork(type: Artwork) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (type) {
            Artwork.MOUNTAINS -> MountainArtwork()
            Artwork.CARDS -> CardsArtwork()
            Artwork.NOTE -> NoteArtwork()
            Artwork.MUGS -> MugsArtwork()
        }
    }
}

@Composable
private fun MountainArtwork() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFFFFC7BB), Color(0xFFF0A5A0), Color(0xFF9B5962))))
        drawCircle(Color(0xFFFFE6BF), radius = size.minDimension * .12f, center = Offset(size.width * .72f, size.height * .27f))
        fun mountain(y: Float, left: Float, peak: Float, right: Float, color: Color) {
            val p = Path().apply {
                moveTo(size.width * left, size.height)
                lineTo(size.width * peak, size.height * y)
                lineTo(size.width * right, size.height)
                close()
            }
            drawPath(p, color)
        }
        mountain(.46f, -.10f, .28f, .70f, Color(0xFF8D5761))
        mountain(.36f, .24f, .58f, 1.12f, Color(0xFF6E4654))
        mountain(.56f, -.12f, .62f, 1.08f, Color(0xFF4D3945))
        drawCircle(Color(0xFFF4B0A8), 5.dp.toPx(), Offset(size.width * .17f, size.height * .82f))
        drawCircle(Color(0xFFF7D0B3), 4.dp.toPx(), Offset(size.width * .29f, size.height * .77f))
        drawCircle(Color(0xFFE89CA2), 4.dp.toPx(), Offset(size.width * .72f, size.height * .81f))
    }
}

@Composable
private fun CardsArtwork() {
    Canvas(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFF1D7CF), Color(0xFFE6B6B2))))) {
        val cw = size.width * .50f
        val ch = size.height * .60f
        drawRoundRect(Color(0xFFE3A39F), Offset(size.width * .18f, size.height * .20f), Size(cw, ch), CornerRadius(14.dp.toPx()))
        drawRoundRect(Color(0xFFB96A73), Offset(size.width * .30f, size.height * .16f), Size(cw, ch), CornerRadius(14.dp.toPx()))
        drawRoundRect(Color(0xFFFFEEE8), Offset(size.width * .39f, size.height * .27f), Size(cw, ch), CornerRadius(14.dp.toPx()))
        val heart = Path().apply {
            moveTo(size.width * .64f, size.height * .49f)
            cubicTo(size.width * .57f, size.height * .41f, size.width * .49f, size.height * .48f, size.width * .64f, size.height * .63f)
            cubicTo(size.width * .79f, size.height * .48f, size.width * .71f, size.height * .41f, size.width * .64f, size.height * .49f)
            close()
        }
        drawPath(heart, EveluneRoseDeep)
    }
}

@Composable
private fun NoteArtwork() {
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFE7CAC0), Color(0xFFD8B0A5), Color(0xFFF0DDD2)))),
        contentAlignment = Alignment.Center
    ) {
        Surface(modifier = Modifier.size(width = 104.dp, height = 112.dp), shape = RoundedCornerShape(6.dp), color = Color(0xFFFFF8F0), shadowElevation = 4.dp) {
            Box(contentAlignment = Alignment.Center) {
                Text("1 · 2 · 3 · 4\nHow true?\n♡", color = Color(0xFF6D4B49), fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun MugsArtwork() {
    Canvas(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE6C5B6), Color(0xFFC99587), Color(0xFF8D625D))))) {
        fun mug(x: Float, color: Color) {
            val left = size.width * x
            val top = size.height * .48f
            val w = size.width * .27f
            val h = size.height * .30f
            drawRoundRect(color, Offset(left, top), Size(w, h), CornerRadius(10.dp.toPx()))
            drawArc(color, -80f, 160f, false, Offset(left + w * .78f, top + h * .13f), Size(w * .52f, h * .58f), style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
            drawArc(Color(0xFFFFF2EA), 220f, 95f, false, Offset(left + w * .28f, top - h * .42f), Size(w * .28f, h * .50f), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        }
        mug(.20f, Color(0xFFD37E73))
        mug(.49f, Color(0xFFB85D61))
        drawCircle(Color(0xFFE7B9A8).copy(alpha = .5f), radius = size.width * .35f, center = Offset(size.width * .72f, size.height * .08f))
    }
}

@Composable
private fun SoftBackdrop() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(Color(0xFFFAD8D4).copy(alpha = .34f), radius = size.width * .55f, center = Offset(size.width * .98f, size.height * .07f))
        drawCircle(Color(0xFFFFE6DF).copy(alpha = .60f), radius = size.width * .40f, center = Offset(size.width * .08f, size.height * .42f))
    }
}

@Composable
private fun EveluneBottomBar(selected: Int, labels: List<String>, onSelect: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFCFA),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
                        Text(label, fontSize = 11.sp, color = tint, fontWeight = if (selected == index) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}
