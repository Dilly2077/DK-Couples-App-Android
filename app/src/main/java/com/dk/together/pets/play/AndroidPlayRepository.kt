package com.dk.together.pets.play

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Durable local play receipts and per-pet cooldown state. */
class AndroidPlayRepository(context: Context) : PlayRepository {
    private val helper = Db(context.applicationContext)

    override fun receipt(interactionId: String): PlayReceipt? {
        helper.readableDatabase.query(
            "play_receipts",
            COLUMNS,
            "interaction_id = ?",
            arrayOf(interactionId),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return PlayReceipt(
                interactionId = cursor.getString(cursor.getColumnIndexOrThrow("interaction_id")),
                petId = cursor.getString(cursor.getColumnIndexOrThrow("pet_id")),
                toy = PlayToy.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("toy"))),
                status = PlayReceiptStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
                startedAtEpochMs = cursor.getLong(cursor.getColumnIndexOrThrow("started_at")),
                completedAtEpochMs = cursor.getColumnIndexOrThrow("completed_at").let { index ->
                    if (cursor.isNull(index)) null else cursor.getLong(index)
                },
            )
        }
    }

    override fun startInteraction(
        interactionId: String,
        petId: String,
        toy: PlayToy,
        nowEpochMs: Long,
    ): PlayReceipt {
        val existing = receipt(interactionId)
        if (existing != null) return existing

        helper.writableDatabase.insertOrThrow(
            "play_receipts",
            null,
            ContentValues().apply {
                put("interaction_id", interactionId)
                put("pet_id", petId)
                put("toy", toy.name)
                put("status", PlayReceiptStatus.STARTED.name)
                put("started_at", nowEpochMs)
                putNull("completed_at")
            },
        )
        return requireNotNull(receipt(interactionId))
    }

    override fun markCompleted(interactionId: String, nowEpochMs: Long) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val current = receipt(interactionId) ?: error("Unknown play interaction: $interactionId")
            if (current.status != PlayReceiptStatus.COMPLETED) {
                db.update(
                    "play_receipts",
                    ContentValues().apply {
                        put("status", PlayReceiptStatus.COMPLETED.name)
                        put("completed_at", nowEpochMs)
                    },
                    "interaction_id = ?",
                    arrayOf(interactionId),
                )
                db.insertWithOnConflict(
                    "pet_play_state",
                    null,
                    ContentValues().apply {
                        put("pet_id", current.petId)
                        put("last_completed_at", nowEpochMs)
                    },
                    SQLiteDatabase.CONFLICT_REPLACE,
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun lastCompletedAt(petId: String): Long? {
        helper.readableDatabase.query(
            "pet_play_state",
            arrayOf("last_completed_at"),
            "pet_id = ?",
            arrayOf(petId),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
    }

    private class Db(context: Context) : SQLiteOpenHelper(context, "evelune_play.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE play_receipts (
                    interaction_id TEXT PRIMARY KEY,
                    pet_id TEXT NOT NULL,
                    toy TEXT NOT NULL,
                    status TEXT NOT NULL,
                    started_at INTEGER NOT NULL,
                    completed_at INTEGER
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE pet_play_state (
                    pet_id TEXT PRIMARY KEY,
                    last_completed_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    private companion object {
        val COLUMNS = arrayOf(
            "interaction_id",
            "pet_id",
            "toy",
            "status",
            "started_at",
            "completed_at",
        )
    }
}
