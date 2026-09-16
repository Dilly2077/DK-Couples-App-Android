package com.dk.together.data

import android.content.Context
import com.dk.together.model.AppPreferences
import com.dk.together.model.CoupleProfile
import com.dk.together.model.PetStats
import com.dk.together.widget.RelationshipWidgetProvider
import kotlinx.coroutines.flow.Flow

class CoupleRepository(private val context: Context) {
    private val store = AppStore(context)
    private val dao = CoupleDatabase.get(context).interactionDao()

    val preferences: Flow<AppPreferences> = store.preferences
    val interactions: Flow<List<InteractionEntity>> = dao.observeAll()

    suspend fun saveProfile(profile: CoupleProfile) {
        store.saveProfile(profile)
        persistWidget(profile.youName, profile.partnerName, profile.startEpochDay, null)
    }

    suspend fun record(type: String, actor: String, title: String, body: String) {
        dao.insert(InteractionEntity(type = type, actor = actor, title = title, body = body))
    }

    suspend fun updateMood(mood: String, partner: Boolean) = store.updateMoodStatus(mood = mood, partner = partner)
    suspend fun updateStatus(status: String, partner: Boolean) = store.updateMoodStatus(status = status, partner = partner)
    suspend fun setDemoAsPartner(value: Boolean) = store.setDemoAsPartner(value)

    suspend fun sendWidgetNote(note: String, prefs: AppPreferences, actor: String) {
        store.setNote(note)
        record("note", actor, "Widget note", note)
        persistWidget(prefs.profile.youName, prefs.profile.partnerName, prefs.profile.startEpochDay, note)
        RelationshipWidgetProvider.updateAll(context)
    }

    suspend fun rewardHearts(current: Int, delta: Int) = store.setHearts(current + delta)

    suspend fun updatePet(stats: PetStats) = store.setPet(stats)

    private fun persistWidget(you: String, partner: String, startEpochDay: Long, note: String?) {
        context.getSharedPreferences("widget_snapshot", Context.MODE_PRIVATE).edit()
            .putString("you", you)
            .putString("partner", partner)
            .putLong("start_epoch_day", startEpochDay)
            .apply {
                if (note != null) putString("note", note)
            }
            .apply()
    }
}
