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

data class ChatMessage(
    val id: Long,
    val threadId: String,
    val actor: LocalActor,
    val body: String,
    val sentAt: Long,
)

data class MemoryEntry(
    val id: Long,
    val title: String,
    val eventDate: Long,
    val description: String,
    val favoritePart: String,
    val createdAt: Long,
)

data class MemoryMedia(
    val id: Long,
    val memoryId: Long,
    val uri: String,
    val mimeType: String,
    val position: Int,
)

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

    fun sendMessage(threadId: String, actor: LocalActor, body: String, at: Long = System.currentTimeMillis()): Long {
        val clean = body.trim()
        require(clean.isNotBlank())
        return helper.writableDatabase.insertOrThrow(
            "chat_messages",
            null,
            ContentValues().apply {
                put("thread_id", threadId)
                put("actor", actor.key)
                put("body", clean)
                put("sent_at", at)
            }
        )
    }

    fun messages(threadId: String): List<ChatMessage> {
        val out = mutableListOf<ChatMessage>()
        helper.readableDatabase.query(
            "chat_messages",
            arrayOf("id", "thread_id", "actor", "body", "sent_at"),
            "thread_id = ?",
            arrayOf(threadId),
            null, null,
            "sent_at ASC, id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                out += ChatMessage(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    threadId = cursor.getString(cursor.getColumnIndexOrThrow("thread_id")),
                    actor = if (cursor.getString(cursor.getColumnIndexOrThrow("actor")) == LocalActor.PARTNER.key) LocalActor.PARTNER else LocalActor.YOU,
                    body = cursor.getString(cursor.getColumnIndexOrThrow("body")),
                    sentAt = cursor.getLong(cursor.getColumnIndexOrThrow("sent_at")),
                )
            }
        }
        return out
    }

    fun saveMemory(
        title: String,
        eventDate: Long,
        description: String,
        favoritePart: String,
        media: List<Pair<String, String>>,
        createdAt: Long = System.currentTimeMillis(),
    ): Long {
        val db = helper.writableDatabase
        db.beginTransaction()
        return try {
            val memoryId = db.insertOrThrow(
                "memories",
                null,
                ContentValues().apply {
                    put("title", title.trim())
                    put("event_date", eventDate)
                    put("description", description.trim())
                    put("favorite_part", favoritePart.trim())
                    put("created_at", createdAt)
                }
            )
            media.forEachIndexed { index, item ->
                db.insertOrThrow(
                    "memory_media",
                    null,
                    ContentValues().apply {
                        put("memory_id", memoryId)
                        put("uri", item.first)
                        put("mime_type", item.second)
                        put("position", index)
                    }
                )
            }
            db.setTransactionSuccessful()
            memoryId
        } finally {
            db.endTransaction()
        }
    }

    fun allMemories(): List<MemoryEntry> {
        val out = mutableListOf<MemoryEntry>()
        helper.readableDatabase.query(
            "memories",
            arrayOf("id", "title", "event_date", "description", "favorite_part", "created_at"),
            null, null, null, null,
            "event_date DESC, created_at DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                out += MemoryEntry(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    eventDate = cursor.getLong(cursor.getColumnIndexOrThrow("event_date")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    favoritePart = cursor.getString(cursor.getColumnIndexOrThrow("favorite_part")),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                )
            }
        }
        return out
    }

    fun mediaForMemory(memoryId: Long): List<MemoryMedia> {
        val out = mutableListOf<MemoryMedia>()
        helper.readableDatabase.query(
            "memory_media",
            arrayOf("id", "memory_id", "uri", "mime_type", "position"),
            "memory_id = ?",
            arrayOf(memoryId.toString()),
            null, null,
            "position ASC, id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                out += MemoryMedia(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    memoryId = cursor.getLong(cursor.getColumnIndexOrThrow("memory_id")),
                    uri = cursor.getString(cursor.getColumnIndexOrThrow("uri")),
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow("mime_type")),
                    position = cursor.getInt(cursor.getColumnIndexOrThrow("position")),
                )
            }
        }
        return out
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

    private class Db(context: Context) : SQLiteOpenHelper(context, "evelune.db", null, 2) {
        override fun onConfigure(db: SQLiteDatabase) {
            super.onConfigure(db)
            db.setForeignKeyConstraintsEnabled(true)
        }

        override fun onCreate(db: SQLiteDatabase) {
            createSubmissions(db)
            createConversationAndTimelineTables(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) createConversationAndTimelineTables(db)
        }

        private fun createSubmissions(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS submissions (
                    content_id TEXT NOT NULL,
                    content_type TEXT NOT NULL,
                    actor TEXT NOT NULL,
                    payload TEXT NOT NULL,
                    submitted_at INTEGER NOT NULL,
                    PRIMARY KEY(content_id, content_type, actor)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_submissions_time ON submissions(submitted_at DESC)")
        }

        private fun createConversationAndTimelineTables(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS chat_messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    thread_id TEXT NOT NULL,
                    actor TEXT NOT NULL,
                    body TEXT NOT NULL,
                    sent_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_chat_thread_time ON chat_messages(thread_id, sent_at ASC)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS memories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    event_date INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    favorite_part TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_memories_event_date ON memories(event_date DESC)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS memory_media (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    memory_id INTEGER NOT NULL,
                    uri TEXT NOT NULL,
                    mime_type TEXT NOT NULL,
                    position INTEGER NOT NULL,
                    FOREIGN KEY(memory_id) REFERENCES memories(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_memory_media ON memory_media(memory_id, position ASC)")
        }
    }
}
