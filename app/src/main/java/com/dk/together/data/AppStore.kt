package com.dk.together.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dk.together.model.AppPreferences
import com.dk.together.model.CoupleProfile
import com.dk.together.model.PetStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "couple_preferences")

class AppStore(private val context: Context) {
    private object Keys {
        val youName = stringPreferencesKey("you_name")
        val partnerName = stringPreferencesKey("partner_name")
        val startEpochDay = longPreferencesKey("start_epoch_day")
        val onboarded = booleanPreferencesKey("onboarded")
        val mood = stringPreferencesKey("mood")
        val status = stringPreferencesKey("status")
        val partnerMood = stringPreferencesKey("partner_mood")
        val partnerStatus = stringPreferencesKey("partner_status")
        val note = stringPreferencesKey("widget_note")
        val hearts = intPreferencesKey("hearts")
        val petName = stringPreferencesKey("pet_name")
        val hunger = intPreferencesKey("pet_hunger")
        val happiness = intPreferencesKey("pet_happiness")
        val cleanliness = intPreferencesKey("pet_cleanliness")
        val energy = intPreferencesKey("pet_energy")
        val affection = intPreferencesKey("pet_affection")
        val petRoom = stringPreferencesKey("pet_room")
        val petLastUpdatedMs = longPreferencesKey("pet_last_updated_ms")
        val demoAsPartner = booleanPreferencesKey("demo_as_partner")
    }

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { p ->
        AppPreferences(
            profile = CoupleProfile(
                youName = p[Keys.youName].orEmpty(),
                partnerName = p[Keys.partnerName].orEmpty(),
                startEpochDay = p[Keys.startEpochDay] ?: java.time.LocalDate.now().toEpochDay(),
                onboarded = p[Keys.onboarded] ?: false
            ),
            mood = p[Keys.mood].orEmpty(),
            status = p[Keys.status].orEmpty(),
            partnerMood = p[Keys.partnerMood].orEmpty(),
            partnerStatus = p[Keys.partnerStatus].orEmpty(),
            widgetNote = p[Keys.note].orEmpty(),
            hearts = p[Keys.hearts] ?: 0,
            petName = p[Keys.petName] ?: "Nova",
            pet = PetStats(
                hunger = p[Keys.hunger] ?: 72,
                happiness = p[Keys.happiness] ?: 82,
                cleanliness = p[Keys.cleanliness] ?: 78,
                energy = p[Keys.energy] ?: 70,
                affection = p[Keys.affection] ?: 86
            ),
            petRoom = p[Keys.petRoom] ?: "Living room",
            petLastUpdatedMs = p[Keys.petLastUpdatedMs] ?: System.currentTimeMillis(),
            demoAsPartner = p[Keys.demoAsPartner] ?: false
        )
    }

    suspend fun saveProfile(profile: CoupleProfile) = context.dataStore.edit { p ->
        p[Keys.youName] = profile.youName
        p[Keys.partnerName] = profile.partnerName
        p[Keys.startEpochDay] = profile.startEpochDay
        p[Keys.onboarded] = true
    }

    suspend fun updateMoodStatus(mood: String? = null, status: String? = null, partner: Boolean = false) = context.dataStore.edit { p ->
        if (partner) {
            mood?.let { p[Keys.partnerMood] = it }
            status?.let { p[Keys.partnerStatus] = it }
        } else {
            mood?.let { p[Keys.mood] = it }
            status?.let { p[Keys.status] = it }
        }
    }

    suspend fun setNote(note: String) = context.dataStore.edit { it[Keys.note] = note }
    suspend fun setHearts(value: Int) = context.dataStore.edit { it[Keys.hearts] = value.coerceAtLeast(0) }
    suspend fun setDemoAsPartner(value: Boolean) = context.dataStore.edit { it[Keys.demoAsPartner] = value }
    suspend fun setPetRoom(room: String) = context.dataStore.edit { it[Keys.petRoom] = room }

    suspend fun setPet(stats: PetStats) = context.dataStore.edit { p ->
        p[Keys.hunger] = stats.hunger.coerceIn(0, 100)
        p[Keys.happiness] = stats.happiness.coerceIn(0, 100)
        p[Keys.cleanliness] = stats.cleanliness.coerceIn(0, 100)
        p[Keys.energy] = stats.energy.coerceIn(0, 100)
        p[Keys.affection] = stats.affection.coerceIn(0, 100)
        p[Keys.petLastUpdatedMs] = System.currentTimeMillis()
    }
}
