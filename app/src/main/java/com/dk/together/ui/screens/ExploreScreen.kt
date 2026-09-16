package com.dk.together.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
    var gameIndex by remember { mutableStateOf(0) }
    var dateIndex by remember { mutableStateOf(0) }
    val categories = listOf("Questions", "Games", "Quizzes", "Challenges", "Date ideas", "Journeys")

    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 28.dp)
    ) {
        item {
            Text("Explore", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            Text("Things to do together, without a paywall.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item { SectionTitle("Browse") }
        items(categories) { category ->
            SoftCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(category, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(when (category) {
                        "Questions" -> "?"
                        "Games" -> "♟"
                        "Quizzes" -> "✓"
                        "Challenges" -> "⚡"
                        "Date ideas" -> "♡"
                        else -> "✦"
                    }, style = MaterialTheme.typography.headlineMedium)
                }
                Text(
                    when (category) {
                        "Questions" -> "100 original prompts across communication, memories, future, values and more."
                        "Games" -> "Quick couple games designed for two answers and easy comparison."
                        "Quizzes" -> "Partner trivia and preference quizzes are scaffolded for expansion."
                        "Challenges" -> "Tiny actions that create real shared moments."
                        "Date ideas" -> "50 original ideas ranging from free at-home dates to small adventures."
                        else -> "Guided multi-step relationship experiences are on the roadmap."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionTitle("Quick game", "This or That") }
        item {
            SoftCard {
                LabelPill("Game")
                Text(thisOrThat[gameIndex], style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { vm.completeChallenge("This or That: ${thisOrThat[gameIndex]} · chose A") }) { Text("Option A") }
                    Button(onClick = { vm.completeChallenge("This or That: ${thisOrThat[gameIndex]} · chose B") }) { Text("Option B") }
                }
                Button(onClick = { gameIndex = (gameIndex + 1) % thisOrThat.size }, modifier = Modifier.fillMaxWidth()) {
                    Text("Next prompt")
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
                Text(idea.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Button(onClick = { dateIndex = (dateIndex + 7) % vm.dateIdeas.size }, modifier = Modifier.fillMaxWidth()) {
                    Text("Spin again")
                }
            }
        }

        item { SectionTitle("Today's challenge") }
        item {
            val challenge = challenges[(java.time.LocalDate.now().dayOfYear % challenges.size)]
            SoftCard {
                Text(challenge, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Button(onClick = { vm.completeChallenge(challenge) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Complete · +8 hearts")
                }
            }
        }
    }
}
