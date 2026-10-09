package com.dk.together.data

import android.content.Context
import com.dk.together.rewards.AndroidRewardRepository
import com.dk.together.rewards.RewardEngine
import com.dk.together.rewards.RewardHooks
import com.dk.together.widget.DuoNoteWidgetProvider
import org.json.JSONArray
import org.json.JSONObject

data class DuoPartnerNote(
    val id: String,
    val sender: LocalActor,
    val text: String,
    val sentAt: Long,
)

class DuoNoteStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val widgetPrefs = appContext.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
    private val rewardHooks = RewardHooks(RewardEngine(AndroidRewardRepository(appContext)))

    fun send(sender: LocalActor, text: String, at: Long = System.currentTimeMillis()): DuoPartnerNote {
        val clean = text.trim()
        require(clean.isNotBlank())
        val note = DuoPartnerNote(
            id = "$at-${sender.key}",
            sender = sender,
            text = clean.take(500),
            sentAt = at,
        )
        val current = all().toMutableList()
        current.add(0, note)
        save(current.take(MAX_NOTES))
        rewardHooks.stickyNoteSent(note.id, sender.key)
        return note
    }

    fun incomingFor(actor: LocalActor): List<DuoPartnerNote> =
        all().filter { it.sender != actor }.sortedByDescending { it.sentAt }

    fun publishWidgetFor(actor: LocalActor) {
        val latest = incomingFor(actor).firstOrNull()
        widgetPrefs.edit()
            .putString("sender", latest?.sender?.display.orEmpty())
            .putString("note", latest?.text.orEmpty())
            .putLong("sent_at", latest?.sentAt ?: 0L)
            .apply()
        DuoNoteWidgetProvider.updateAll(appContext)
    }

    private fun all(): List<DuoPartnerNote> {
        val raw = prefs.getString(KEY_NOTES, "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val senderKey = item.optString("sender")
                    add(
                        DuoPartnerNote(
                            id = item.optString("id"),
                            sender = if (senderKey == LocalActor.PARTNER.key) LocalActor.PARTNER else LocalActor.YOU,
                            text = item.optString("text"),
                            sentAt = item.optLong("sent_at"),
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun save(notes: List<DuoPartnerNote>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(
                JSONObject()
                    .put("id", note.id)
                    .put("sender", note.sender.key)
                    .put("text", note.text)
                    .put("sent_at", note.sentAt)
            )
        }
        prefs.edit().putString(KEY_NOTES, array.toString()).apply()
    }

    companion object {
        private const val PREFS = "duo_partner_notes"
        private const val WIDGET_PREFS = "duo_note_widget"
        private const val KEY_NOTES = "notes"
        private const val MAX_NOTES = 100
    }
}
