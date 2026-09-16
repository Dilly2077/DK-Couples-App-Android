package com.dk.together.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

enum class LocalActor(val key: String, val display: String) {
    YOU("you", "You"),
    PARTNER("partner", "Partner");

    fun other(): LocalActor = if (this == YOU) PARTNER else YOU
}

data class Submission(
    val contentId: String,
    val contentType: String,
    val actor: LocalActor,
    val payload: String,
    val submittedAt: Long,
)

data class PairSubmission(
    val you: Submission?,
    val partner: Submission?,
) {
    val bothSubmitted: Boolean get() = you != null && partner != null
    val latestAt: Long? get() = listOfNotNull(you?.submittedAt, partner?.submittedAt).maxOrNull()
}

class EveluneStore(context: Context) {
    companion object {
        const val TYPE_QUESTION = "question"
        const val TYPE_CARD = "card"
        const val TYPE_CHALLENGE = "challenge"
    }

    private val appContext = context.applicationContext
    private val helper = Db(appContext)
    private val prefs = appContext.getSharedPreferences("evelune_preview", Context.MODE_PRIVATE)

    var currentActor: LocalActor
        get() = if (prefs.getString("actor", LocalActor.YOU.key) == LocalActor.PARTNER.key) LocalActor.PARTNER else LocalActor.YOU
        set(value) { prefs.edit().putString("actor", value.key).apply() }

    var partnerName: String
        get() = prefs.getString("partner_name", "your partner")?.ifBlank { "your partner" } ?: "your partner"
        set(value) { prefs.edit().putString("partner_name", value.trim().ifBlank { "your partner" }).apply() }

    fun save(contentId: String, contentType: String, actor: LocalActor, payload: String, at: Long = System.currentTimeMillis()) {
        val values = ContentValues().apply {
            put("content_id", contentId)
            put("content_type", contentType)
            put("actor", actor.key)
            put("payload", payload)
            put("submitted_at", at)
        }
        helper.writableDatabase.insertWithOnConflict("submissions", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun pair(contentId: String, contentType: String): PairSubmission {
        var you: Submission? = null
        var partner: Submission? = null
        helper.readableDatabase.query(
            "submissions",
            arrayOf("content_id", "content_type", "actor", "payload", "submitted_at"),
            "content_id = ? AND content_type = ?",
            arrayOf(contentId, contentType),
            null, null, null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val row = cursor.toSubmission()
                if (row.actor == LocalActor.YOU) you = row else partner = row
            }
        }
        return PairSubmission(you, partner)
    }

    fun allSubmissions(): List<Submission> {
        val out = mutableListOf<Submission>()
        helper.readableDatabase.query(
            "submissions",
            arrayOf("content_id", "content_type", "actor", "payload", "submitted_at"),
            null, null, null, null,
            "submitted_at DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) out += cursor.toSubmission()
        }
        return out
    }

    fun completedBy(contentId: String, contentType: String, actor: LocalActor): Submission? {
        helper.readableDatabase.query(
            "submissions",
            arrayOf("content_id", "content_type", "actor", "payload", "submitted_at"),
            "content_id = ? AND content_type = ? AND actor = ?",
            arrayOf(contentId, contentType, actor.key),
            null, null, null,
            "1"
        ).use { cursor -> return if (cursor.moveToFirst()) cursor.toSubmission() else null }
    }

    private fun android.database.Cursor.toSubmission(): Submission {
        val actor = if (getString(getColumnIndexOrThrow("actor")) == LocalActor.PARTNER.key) LocalActor.PARTNER else LocalActor.YOU
        return Submission(
            contentId = getString(getColumnIndexOrThrow("content_id")),
            contentType = getString(getColumnIndexOrThrow("content_type")),
            actor = actor,
            payload = getString(getColumnIndexOrThrow("payload")),
            submittedAt = getLong(getColumnIndexOrThrow("submitted_at")),
        )
    }

    private class Db(context: Context) : SQLiteOpenHelper(context, "evelune.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE submissions (
                    content_id TEXT NOT NULL,
                    content_type TEXT NOT NULL,
                    actor TEXT NOT NULL,
                    payload TEXT NOT NULL,
                    submitted_at INTEGER NOT NULL,
                    PRIMARY KEY(content_id, content_type, actor)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX idx_submissions_time ON submissions(submitted_at DESC)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }
}
