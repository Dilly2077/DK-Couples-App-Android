package com.dk.together.data

import android.content.Context
import com.dk.together.model.AppPreferences
import com.dk.together.model.CoupleProfile
import com.dk.together.widget.DailyQuestionWidgetProvider
import com.dk.together.widget.RelationshipWidgetProvider
import kotlinx.coroutines.flow.Flow

class CoupleRepository(private val context: Context) {
    private val store = AppStore(context)
    private val dao = CoupleDatabase.get(context).interactionDao()

    val preferences: Flow<AppPreferences> = store.preferences
    val interactions: Flow<List<InteractionEntity>> = dao.observeAll()

    suspend fun saveProfile(profile: CoupleProfile) {
        store.saveProfile(profile)
        persistWidgetProfile(profile.youName, profile.partnerName, profile.startEpochDay)
        RelationshipWidgetProvider.updateAll(context)
        DailyQuestionWidgetProvider.updateAll(context)
    }

    suspend fun record(type: String, actor: String, title: String, body: String) {
        dao.insert(InteractionEntity(type = type, actor = actor, title = title, body = body))
    }

    suspend fun setDemoAsPartner(value: Boolean) = store.setDemoAsPartner(value)

    suspend fun sendWidgetNote(note: String, prefs: AppPreferences, actor: String) {
        store.setNote(note)
        record("note", actor, "Sticky note", note)
        val snapshot = context.getSharedPreferences("widget_snapshot", Context.MODE_PRIVATE)
        snapshot.edit()
            .putString("you", prefs.profile.youName)
            .putString("partner", prefs.profile.partnerName)
            .putLong("start_epoch_day", prefs.profile.startEpochDay)
            .putString("note", note)
            .putString("note_sender", actor)
            .putLong("note_time", System.currentTimeMillis())
            .apply()
        RelationshipWidgetProvider.updateAll(context)
    }

    private fun persistWidgetProfile(you: String, partner: String, startEpochDay: Long) {
        context.getSharedPreferences("widget_snapshot", Context.MODE_PRIVATE).edit()
            .putString("you", you)
            .putString("partner", partner)
            .putLong("start_epoch_day", startEpochDay)
            .apply()
    }
}
