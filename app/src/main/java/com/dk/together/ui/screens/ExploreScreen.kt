package com.dk.together.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.ui.AccentCard
import com.dk.together.ui.AppUiState
import com.dk.together.ui.AppViewModel
import com.dk.together.ui.LabelPill
import com.dk.together.ui.SectionTitle
import com.dk.together.ui.SoftCard
import kotlin.random.Random

private val thisOrThat = listOf(
    "Sunrise date or midnight walk?",
    "Fancy restaurant or street-food crawl?",
    "Beach weekend or cabin weekend?",
    "Plan everything or decide on the day?",
    "Board-game night or movie marathon?",
    "Photos everywhere or live in the moment?",
    "Cook together or order something new?",
    "City break or countryside escape?"
)

private val challenges = listOf(
    "Give your partner one specific compliment you have never said before.",
    "Take a photo together that captures an ordinary moment.",
    "Plan a date for £20 or less.",
    "Recreate one old photo together.",
    "Put both phones away for a 20-minute walk.",
    "Make your partner their favourite drink without being asked."
)

@Composable
fun ExploreScreen(state: AppUiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    var gameIndex by remember { mutableIntStateOf(0) }
    var dateIndex by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("Games") }
    val categories = listOf("Games", "Questions", "Quizzes", "Challenges", "Dates", "Journeys")

    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 30.dp)
    ) {
        item {
            Text("Explore", style = MaterialTheme.typography.displaySmall)
            Text("Play, talk and make small things together.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }
        }

        item {
            AccentCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        LabelPill("Mini-game")
                        Text("Fruit Merge", style = MaterialTheme.typography.headlineSmall)
                    }
                    Text("🍓", fontSize = 34.sp)
                }
                Text(
                    "Drop fruit into the basket. Matching fruit touching at the top merge into the next fruit. Take turns and bank your score to the shared history.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FruitMergeGame(state = state, vm = vm)
            }
        }

        item { SectionTitle("Quick couple game", "This or That") }
        item {
            SoftCard {
                LabelPill("Question ${gameIndex + 1}/${thisOrThat.size}")
                Text(thisOrThat[gameIndex], style = MaterialTheme.typography.headlineSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { vm.recordGameScore("This or That", 10); gameIndex = (gameIndex + 1) % thisOrThat.size },
                        modifier = Modifier.weight(1f)
                    ) { Text("First") }
                    OutlinedButton(
                        onClick = { vm.recordGameScore("This or That", 10); gameIndex = (gameIndex + 1) % thisOrThat.size },
                        modifier = Modifier.weight(1f)
                    ) { Text("Second") }
                }
            }
        }

        item { SectionTitle("Date roulette") }
        item {
            val idea = vm.dateIdeas[dateIndex]
            SoftCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelPill(idea.category)
                    LabelPill(idea.cost)
                }
                Text(idea.title, style = MaterialTheme.typography.headlineSmall)
                Button(onClick = { dateIndex = (dateIndex + 7) % vm.dateIdeas.size }, modifier = Modifier.fillMaxWidth()) {
                    Text("Spin again")
                }
            }
        }

        item { SectionTitle("Today's challenge") }
        item {
            val challenge = challenges[java.time.LocalDate.now().dayOfYear % challenges.size]
            SoftCard {
                Text(challenge, style = MaterialTheme.typography.titleLarge)
                Button(onClick = { vm.completeChallenge(challenge) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Complete · +8 hearts")
                }
            }
        }
    }
}

@Composable
private fun FruitMergeGame(state: AppUiState, vm: AppViewModel) {
    val fruit = listOf("🍒", "🍓", "🍊", "🍎", "🍐", "🍑", "🍍", "🍉")
    val stack = remember { mutableStateListOf<Int>() }
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    val actor = if (state.prefs.demoAsPartner) state.prefs.profile.partnerName else state.prefs.profile.youName

    fun mergeTop() {
        var keepMerging = true
        while (keepMerging && stack.size >= 2) {
            val last = stack.lastIndex
            if (stack[last] == stack[last - 1]) {
                val next = (stack[last] + 1).coerceAtMost(fruit.lastIndex)
                stack.removeAt(last)
                stack.removeAt(last - 1)
                stack.add(next)
                score += (next + 1) * 10
            } else {
                keepMerging = false
            }
        }
    }

    fun dropFruit() {
        if (gameOver) return
        stack.add(Random.nextInt(0, 3))
        mergeTop()
        if (stack.size >= 10) gameOver = true
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Playing as ${actor.ifBlank { "You" }}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            LabelPill("Score $score")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.background.copy(alpha = .48f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (stack.isEmpty()) {
                Text("Drop a fruit to start", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(
                    modifier = Modifier.padding(bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((-4).dp)
                ) {
                    stack.asReversed().take(9).asReversed().forEach { level ->
                        Text(fruit[level], fontSize = (32 + level * 2).sp)
                    }
                }
            }
            Text("basket", modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (gameOver) {
            Text("Basket full — bank your score or reset.", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = ::dropFruit, enabled = !gameOver, modifier = Modifier.weight(1f)) { Text("Drop fruit") }
            OutlinedButton(
                onClick = {
                    vm.recordGameScore("Fruit Merge", score)
                    stack.clear()
                    score = 0
                    gameOver = false
                },
                enabled = score > 0,
                modifier = Modifier.weight(1f)
            ) { Text("Bank score") }
        }
    }
}
