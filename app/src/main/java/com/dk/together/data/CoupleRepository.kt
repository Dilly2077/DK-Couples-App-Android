package com.dk.together.data

import android.content.Context
import com.dk.together.model.AppPreferences
import com.dk.together.model.CoupleProfile
import com.dk.together.model.PetStats
import com.dk.together.widget.PetWidgetProvider
import com.dk.together.widget.RelationshipWidgetProvider
import kotlinx.coroutines.flow.Flow

class CoupleRepository(private val context: Context) {
    private val store = AppStore(context)
    private val dao = CoupleDatabase.get(context).interactionDao()

    val preferences: Flow<AppPreferences> = store.preferences
    val interactions: Flow<List<InteractionEntity>> = dao.observeAll()

    suspend fun saveProfile(profile: CoupleProfile) {
        store.saveProfile(profile)
        persistRelationshipWidget(profile.youName, profile.partnerName, profile.startEpochDay, null)
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
        persistRelationshipWidget(prefs.profile.youName, prefs.profile.partnerName, prefs.profile.startEpochDay, note)
        RelationshipWidgetProvider.updateAll(context)
    }

    suspend fun rewardHearts(current: Int, delta: Int) = store.setHearts(current + delta)

    suspend fun updatePet(stats: PetStats, prefs: AppPreferences) {
        store.setPet(stats)
        persistPetWidget(prefs.petName, prefs.petRoom, stats)
        PetWidgetProvider.updateAll(context)
    }

    suspend fun updatePetRoom(room: String, prefs: AppPreferences) {
        store.setPetRoom(room)
        persistPetWidget(prefs.petName, room, prefs.pet)
        PetWidgetProvider.updateAll(context)
    }

    private fun persistRelationshipWidget(you: String, partner: String, startEpochDay: Long, note: String?) {
        context.getSharedPreferences("widget_snapshot", Context.MODE_PRIVATE).edit()
            .putString("you", you)
            .putString("partner", partner)
            .putLong("start_epoch_day", startEpochDay)
            .apply {
                if (note != null) putString("note", note)
            }
            .apply()
    }

    private fun persistPetWidget(name: String, room: String, stats: PetStats) {
        context.getSharedPreferences("widget_snapshot", Context.MODE_PRIVATE).edit()
            .putString("pet_name", name)
            .putString("pet_room", room)
            .putInt("pet_hunger", stats.hunger)
            .putInt("pet_happiness", stats.happiness)
            .putInt("pet_cleanliness", stats.cleanliness)
            .putInt("pet_energy", stats.energy)
            .putInt("pet_affection", stats.affection)
            .apply()
    }
}
