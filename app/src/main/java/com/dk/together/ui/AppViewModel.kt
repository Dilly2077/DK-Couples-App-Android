package com.dk.together.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dk.together.data.CoupleRepository
import com.dk.together.data.InteractionEntity
import com.dk.together.model.AppPreferences
import com.dk.together.model.CoupleProfile
import com.dk.together.model.DateIdea
import com.dk.together.model.PetMath
import com.dk.together.model.PetStats
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
        AppUiState(
            prefs.copy(pet = PetMath.decayed(prefs.pet, prefs.petLastUpdatedMs)),
            interactions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    val questions: List<QuestionContent> by lazy { loadQuestions() }
    val dateIdeas: List<DateIdea> by lazy { loadDateIdeas() }
    val dailyQuestion: QuestionContent get() = questions[(LocalDate.now().toEpochDay() % questions.size).toInt()]

    fun saveProfile(you: String, partner: String, start: LocalDate) {
        viewModelScope.launch {
            repo.saveProfile(CoupleProfile(you.trim(), partner.trim(), start.toEpochDay(), true))
            repo.record("milestone", you.ifBlank { "You" }, "Our space began", "${you.trim()} + ${partner.trim()}")
        }
    }

    fun answerDaily(answer: String) {
        if (answer.isBlank()) return
        val prefs = state.value.prefs
        viewModelScope.launch {
            repo.record("question", actorName(prefs), dailyQuestion.prompt, answer.trim())
            repo.rewardHearts(prefs.hearts, 5)
        }
    }

    fun sendNote(note: String) {
        if (note.isBlank()) return
        val prefs = state.value.prefs
        viewModelScope.launch {
            repo.sendWidgetNote(note.trim(), prefs, actorName(prefs))
            repo.rewardHearts(prefs.hearts, 2)
        }
    }

    fun addMemory(title: String, body: String) {
        if (title.isBlank()) return
        val prefs = state.value.prefs
        viewModelScope.launch {
            repo.record("memory", actorName(prefs), title.trim(), body.trim())
            repo.rewardHearts(prefs.hearts, 3)
        }
    }

    fun setMood(mood: String) {
        val prefs = state.value.prefs
        viewModelScope.launch {
            repo.updateMood(mood, prefs.demoAsPartner)
            repo.record("mood", actorName(prefs), "Mood", mood)
        }
    }

    fun setStatus(status: String) {
        val prefs = state.value.prefs
        viewModelScope.launch {
            repo.updateStatus(status, prefs.demoAsPartner)
            repo.record("status", actorName(prefs), "Status", status)
        }
    }

    fun toggleDemoPartner() {
        val next = !state.value.prefs.demoAsPartner
        viewModelScope.launch { repo.setDemoAsPartner(next) }
    }

    fun setPetRoom(room: String) {
        val p = state.value.prefs
        if (room == p.petRoom) return
        viewModelScope.launch {
            repo.updatePetRoom(room, p)
            repo.record("pet", actorName(p), "${p.petName} moved", room)
        }
    }

    fun petAction(action: String) {
        val p = state.value.prefs
        val s = p.pet
        val updated = when (action) {
            "Feed" -> s.copy(hunger = s.hunger + 24, happiness = s.happiness + 3)
            "Treat" -> s.copy(hunger = s.hunger + 12, happiness = s.happiness + 8)
            "Play" -> s.copy(happiness = s.happiness + 18, energy = s.energy - 8, affection = s.affection + 5)
            "Wash", "Bath" -> s.copy(cleanliness = s.cleanliness + 30, happiness = s.happiness - 1)
            "Splash" -> s.copy(cleanliness = s.cleanliness + 12, happiness = s.happiness + 10)
            "Nap" -> s.copy(energy = s.energy + 28, hunger = s.hunger - 5)
            "Cuddle" -> s.copy(affection = s.affection + 20, happiness = s.happiness + 8)
            "Explore" -> s.copy(happiness = s.happiness + 14, energy = s.energy - 10, affection = s.affection + 3)
            else -> s
        }.clamp()
        viewModelScope.launch {
            repo.updatePet(updated, p)
            repo.record("pet", actorName(p), "$action ${p.petName}", petReaction(updated))
            repo.rewardHearts(p.hearts, 1)
        }
    }

    fun completeChallenge(title: String) {
        val p = state.value.prefs
        viewModelScope.launch {
            repo.record("challenge", actorName(p), "Challenge complete", title)
            repo.rewardHearts(p.hearts, 8)
        }
    }

    fun recordGameScore(game: String, score: Int) {
        val p = state.value.prefs
        viewModelScope.launch {
            repo.record("game", actorName(p), game, "Score: $score")
            repo.rewardHearts(p.hearts, (score / 50).coerceIn(1, 12))
        }
    }

    private fun actorName(prefs: AppPreferences): String =
        if (prefs.demoAsPartner) prefs.profile.partnerName.ifBlank { "Partner" }
        else prefs.profile.youName.ifBlank { "You" }

    private fun PetStats.clamp() = copy(
        hunger = hunger.coerceIn(0, 100),
        happiness = happiness.coerceIn(0, 100),
        cleanliness = cleanliness.coerceIn(0, 100),
        energy = energy.coerceIn(0, 100),
        affection = affection.coerceIn(0, 100)
    )

    private fun petReaction(stats: PetStats): String = when {
        stats.happiness > 90 -> "is bouncing around happily ✨"
        stats.energy < 30 -> "looks ready for a nap 💤"
        stats.hunger < 30 -> "is thinking very seriously about snacks"
        stats.cleanliness < 30 -> "would really like a wash"
        else -> "seems content in your shared little world"
    }

    private fun loadQuestions(): List<QuestionContent> {
        val raw = getApplication<Application>().assets.open("questions.json").bufferedReader().use { it.readText() }
        val array = JSONArray(raw)
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            QuestionContent(o.getString("id"), o.getString("category"), o.getString("prompt"))
        }
    }

    private fun loadDateIdeas(): List<DateIdea> {
        val raw = getApplication<Application>().assets.open("date_ideas.json").bufferedReader().use { it.readText() }
        val array = JSONArray(raw)
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            DateIdea(o.getString("title"), o.getString("category"), o.getString("cost"))
        }
    }
}
