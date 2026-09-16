package com.dk.together.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dk.together.data.CoupleRepository
import com.dk.together.data.InteractionEntity
import com.dk.together.model.AppPreferences
import com.dk.together.model.CardContent
import com.dk.together.model.ContentBanks
import com.dk.together.model.CoupleProfile
import com.dk.together.model.DateIdea
import com.dk.together.model.GamePrompt
import com.dk.together.model.QuestionContent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate

data class AppUiState(
    val prefs: AppPreferences = AppPreferences(),
    val interactions: List<InteractionEntity> = emptyList()
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = CoupleRepository(application)

    val state = combine(repo.preferences, repo.interactions) { prefs, interactions ->
        AppUiState(prefs, interactions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    val questions: List<QuestionContent> by lazy { loadQuestions() }
    val cards: List<CardContent> by lazy { ContentBanks.cards(questions) }
    val gamePrompts: List<GamePrompt> by lazy { ContentBanks.games() }
    val dateIdeas: List<DateIdea> by lazy { loadDateIdeas() }

    private val todayEpochDay: Long get() = LocalDate.now().toEpochDay()
    val dailyQuestion: QuestionContent get() = questions[floorIndex(todayEpochDay, questions.size)]
    val dailyCards: List<CardContent> get() = deterministicSelection(cards, count = 3, salt = 97L)
    val dailyGame: List<GamePrompt> get() = deterministicSelection(gamePrompts, count = 5, salt = 211L)

    fun dailyQuestionKey(): String = "$todayEpochDay:${dailyQuestion.id}"
    fun cardKey(card: CardContent): String = "$todayEpochDay:${card.id}"
    fun gameKey(prompt: GamePrompt): String = "$todayEpochDay:${prompt.id}"

    fun currentActor(prefs: AppPreferences = state.value.prefs): String =
        if (prefs.demoAsPartner) prefs.profile.partnerName.ifBlank { "Partner" }
        else prefs.profile.youName.ifBlank { "You" }

    fun otherActor(prefs: AppPreferences = state.value.prefs): String =
        if (prefs.demoAsPartner) prefs.profile.youName.ifBlank { "You" }
        else prefs.profile.partnerName.ifBlank { "Partner" }

    fun saveProfile(you: String, partner: String, start: LocalDate) {
        viewModelScope.launch {
            repo.saveProfile(CoupleProfile(you.trim(), partner.trim(), start.toEpochDay(), true))
        }
    }

    fun answerDaily(answer: String) {
        if (answer.isBlank()) return
        val p = state.value.prefs
        val actor = currentActor(p)
        val key = dailyQuestionKey()
        if (hasResponse("daily_answer", key, actor)) return
        viewModelScope.launch { repo.record("daily_answer", actor, key, answer.trim()) }
    }

    fun answerCard(card: CardContent, answer: String) {
        if (answer.isBlank()) return
        val p = state.value.prefs
        val actor = currentActor(p)
        val key = cardKey(card)
        if (hasResponse("card_answer", key, actor)) return
        viewModelScope.launch { repo.record("card_answer", actor, key, answer.trim()) }
    }

    fun answerGame(prompt: GamePrompt, ownChoice: String, partnerGuess: String) {
        if (ownChoice !in setOf("A", "B") || partnerGuess !in setOf("A", "B")) return
        val p = state.value.prefs
        val actor = currentActor(p)
        val key = gameKey(prompt)
        if (hasResponse("game_pick", key, actor)) return
        viewModelScope.launch {
            repo.record("game_pick", actor, key, "own=$ownChoice;guess=$partnerGuess")
        }
    }

    fun sendNote(note: String) {
        if (note.isBlank()) return
        val p = state.value.prefs
        viewModelScope.launch { repo.sendWidgetNote(note.trim(), p, currentActor(p)) }
    }

    fun addSpecialDate(title: String, date: LocalDate, detail: String) {
        if (title.isBlank()) return
        val actor = currentActor()
        val body = "${date.toEpochDay()}|${detail.trim()}"
        viewModelScope.launch { repo.record("special_date", actor, title.trim(), body) }
    }

    fun toggleDemoPartner() {
        val next = !state.value.prefs.demoAsPartner
        viewModelScope.launch { repo.setDemoAsPartner(next) }
    }

    fun response(type: String, key: String, actor: String): InteractionEntity? =
        state.value.interactions.firstOrNull { it.type == type && it.title == key && it.actor == actor }

    fun hasResponse(type: String, key: String, actor: String): Boolean = response(type, key, actor) != null

    fun questionByKey(key: String): QuestionContent? {
        val id = key.substringAfter(':', "")
        return questions.firstOrNull { it.id == id }
    }

    fun cardByKey(key: String): CardContent? {
        val id = key.substringAfter(':', "")
        return cards.firstOrNull { it.id == id }
    }

    fun gameByKey(key: String): GamePrompt? {
        val id = key.substringAfter(':', "")
        return gamePrompts.firstOrNull { it.id == id }
    }

    private fun floorIndex(value: Long, size: Int): Int {
        if (size <= 0) return 0
        val mod = value % size
        return (if (mod < 0) mod + size else mod).toInt()
    }

    private fun <T> deterministicSelection(source: List<T>, count: Int, salt: Long): List<T> {
        if (source.isEmpty()) return emptyList()
        val start = floorIndex(todayEpochDay * salt + 17L, source.size)
        val stride = ((salt.toInt() % (source.size - 1).coerceAtLeast(1)) + 1).coerceAtLeast(1)
        val out = ArrayList<T>(count)
        var index = start
        repeat(count.coerceAtMost(source.size)) {
            while (out.contains(source[index])) index = (index + 1) % source.size
            out += source[index]
            index = (index + stride) % source.size
        }
        return out
    }

    private fun loadQuestions(): List<QuestionContent> {
        val array = loadArray("questions.json")
        val base = List(array.length()) { i ->
            val o = array.getJSONObject(i)
            QuestionContent(o.getString("id"), o.getString("category"), o.getString("prompt"))
        }
        return ContentBanks.expandedQuestions(base)
    }

    private fun loadDateIdeas(): List<DateIdea> {
        val array = loadArray("date_ideas.json")
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            DateIdea(o.getString("title"), o.getString("category"), o.getString("cost"))
        }
    }

    private fun loadArray(file: String): JSONArray {
        val raw = getApplication<Application>().assets.open(file).bufferedReader().use { it.readText() }
        return JSONArray(raw)
    }
}
