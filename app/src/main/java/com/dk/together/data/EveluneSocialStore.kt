package com.dk.together.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dk.together.rewards.AndroidRewardRepository
import com.dk.together.rewards.RewardEngine
import com.dk.together.rewards.RewardHooks

data class QuestionMessage(
    val id: Long,
    val questionId: String,
    val actor: LocalActor,
    val text: String,
    val sentAt: Long,
)

data class MemoryMoment(
    val id: Long,
    val dateMillis: Long,
    val title: String,
    val description: String,
    val whySpecial: String,
    val favouritePart: String,
    val mediaUris: List<String>,
    val createdAt: Long,
)

class EveluneSocialStore(context: Context) {
    private val appContext = context.applicationContext
    private val helper = Db(appContext)
    private val rewardHooks = RewardHooks(RewardEngine(AndroidRewardRepository(appContext)))

    fun addMessage(
        questionId: String,
        actor: LocalActor,
        text: String,
        at: Long = System.currentTimeMillis(),
    ): Long {
        val clean = text.trim()
        require(clean.isNotBlank())
        val values = ContentValues().apply {
            put("question_id", questionId)
            put("actor", actor.key)
            put("text", clean)
            put("sent_at", at)
        }
        val id = helper.writableDatabase.insertOrThrow("question_messages", null, values)
        rewardHooks.questionChatMessage(questionId, id, actor.key)
        return id
    }

    fun messages(questionId: String): List<QuestionMessage> {
        val out = mutableListOf<QuestionMessage>()
        helper.readableDatabase.query(
            "question_messages",
            arrayOf("id", "question_id", "actor", "text", "sent_at"),
            "question_id = ?",
            arrayOf(questionId),
            null,
            null,
            "sent_at ASC, id ASC",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                out += QuestionMessage(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    questionId = cursor.getString(cursor.getColumnIndexOrThrow("question_id")),
                    actor = if (cursor.getString(cursor.getColumnIndexOrThrow("actor")) == LocalActor.PARTNER.key) LocalActor.PARTNER else LocalActor.YOU,
                    text = cursor.getString(cursor.getColumnIndexOrThrow("text")),
                    sentAt = cursor.getLong(cursor.getColumnIndexOrThrow("sent_at")),
                )
            }
        }
        return out
    }

    fun latestMessage(questionId: String): QuestionMessage? {
        helper.readableDatabase.query(
            "question_messages",
            arrayOf("id", "question_id", "actor", "text", "sent_at"),
            "question_id = ?",
            arrayOf(questionId),
            null,
            null,
            "sent_at DESC, id DESC",
            "1",
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return QuestionMessage(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                questionId = cursor.getString(cursor.getColumnIndexOrThrow("question_id")),
                actor = if (cursor.getString(cursor.getColumnIndexOrThrow("actor")) == LocalActor.PARTNER.key) LocalActor.PARTNER else LocalActor.YOU,
                text = cursor.getString(cursor.getColumnIndexOrThrow("text")),
                sentAt = cursor.getLong(cursor.getColumnIndexOrThrow("sent_at")),
            )
        }
    }

    fun addMemory(
        dateMillis: Long,
        title: String,
        description: String,
        whySpecial: String,
        favouritePart: String,
        mediaUris: List<String>,
        at: Long = System.currentTimeMillis(),
    ): Long {
        val values = ContentValues().apply {
            put("date_millis", dateMillis)
            put("title", title.trim())
            put("description", description.trim())
            put("why_special", whySpecial.trim())
            put("favourite_part", favouritePart.trim())
            put("media_uris", mediaUris.joinToString(URI_SEPARATOR))
            put("created_at", at)
        }
        val id = helper.writableDatabase.insertOrThrow("memories", null, values)
        val meaningful = title.isNotBlank() || description.isNotBlank() || whySpecial.isNotBlank() || favouritePart.isNotBlank() || mediaUris.isNotEmpty()
        rewardHooks.memoryCreated("social-$id", meaningful = meaningful)
        return id
    }

    fun memories(): List<MemoryMoment> {
        val out = mutableListOf<MemoryMoment>()
        helper.readableDatabase.query(
            "memories",
            arrayOf("id", "date_millis", "title", "description", "why_special", "favourite_part", "media_uris", "created_at"),
            null,
            null,
            null,
            null,
            "date_millis DESC, created_at DESC",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                out += MemoryMoment(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("date_millis")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    whySpecial = cursor.getString(cursor.getColumnIndexOrThrow("why_special")),
                    favouritePart = cursor.getString(cursor.getColumnIndexOrThrow("favourite_part")),
                    mediaUris = cursor.getString(cursor.getColumnIndexOrThrow("media_uris"))
                        .split(URI_SEPARATOR)
                        .filter { it.isNotBlank() },
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                )
            }
        }
        return out
    }

    companion object {
        private const val URI_SEPARATOR = "\u001F"
    }

    private class Db(context: Context) : SQLiteOpenHelper(context, "evelune_social.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE question_messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    question_id TEXT NOT NULL,
                    actor TEXT NOT NULL,
                    text TEXT NOT NULL,
                    sent_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX idx_question_messages_thread ON question_messages(question_id, sent_at)")
            db.execSQL(
                """
                CREATE TABLE memories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date_millis INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    why_special TEXT NOT NULL,
                    favourite_part TEXT NOT NULL,
                    media_uris TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX idx_memories_date ON memories(date_millis DESC)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }
}
