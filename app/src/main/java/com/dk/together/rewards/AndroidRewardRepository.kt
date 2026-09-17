package com.dk.together.rewards

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AndroidRewardRepository(context: Context) : RewardRepository {
    private val helper = Db(context.applicationContext)

    override fun loadBalance(): RewardBalance {
        helper.readableDatabase.query(
            "reward_wallet",
            arrayOf("evelune_xp", "pet_coins"),
            "id = 1",
            null,
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (!cursor.moveToFirst()) return RewardBalance()
            return RewardBalance(
                eveluneXp = cursor.getInt(cursor.getColumnIndexOrThrow("evelune_xp")),
                petCoins = cursor.getInt(cursor.getColumnIndexOrThrow("pet_coins")),
            )
        }
    }

    override fun loadPetBondXp(petId: String): Int {
        helper.readableDatabase.query(
            "pet_progress",
            arrayOf("bond_xp"),
            "pet_id = ?",
            arrayOf(petId),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow("bond_xp")) else 0
        }
    }

    override fun hasEvent(eventId: String): Boolean {
        helper.readableDatabase.query(
            "reward_events",
            arrayOf("event_id"),
            "event_id = ?",
            arrayOf(eventId),
            null,
            null,
            null,
            "1",
        ).use { cursor -> return cursor.moveToFirst() }
    }

    override fun countGrantedEvents(
        type: RewardEventType,
        actorKey: String?,
        petId: String?,
        sinceEpochMs: Long,
    ): Int {
        val clauses = mutableListOf(
            "event_type = ?",
            "decision = ?",
            "created_at >= ?",
        )
        val args = mutableListOf(
            type.name,
            RewardDecision.GRANTED.name,
            sinceEpochMs.toString(),
        )
        if (actorKey != null) {
            clauses += "actor_key = ?"
            args += actorKey
        }
        if (petId != null) {
            clauses += "pet_id = ?"
            args += petId
        }

        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM reward_events WHERE ${clauses.joinToString(" AND ")}",
            args.toTypedArray(),
        ).use { cursor ->
            cursor.moveToFirst()
            return cursor.getInt(0)
        }
    }

    override fun record(entry: RewardLedgerEntry): Boolean {
        val db = helper.writableDatabase
        db.beginTransaction()
        return try {
            val rowId = db.insertWithOnConflict(
                "reward_events",
                null,
                ContentValues().apply {
                    put("event_id", entry.eventId)
                    put("event_type", entry.eventType.name)
                    if (entry.actorKey == null) putNull("actor_key") else put("actor_key", entry.actorKey)
                    if (entry.petId == null) putNull("pet_id") else put("pet_id", entry.petId)
                    put("decision", entry.decision.name)
                    put("evelune_xp", entry.grant.eveluneXp)
                    put("pet_bond_xp", entry.grant.petBondXp)
                    put("pet_coins", entry.grant.petCoins)
                    put("created_at", entry.createdAtEpochMs)
                },
                SQLiteDatabase.CONFLICT_IGNORE,
            )
            if (rowId == -1L) {
                false
            } else {
                db.execSQL(
                    "UPDATE reward_wallet SET evelune_xp = evelune_xp + ?, pet_coins = pet_coins + ? WHERE id = 1",
                    arrayOf(entry.grant.eveluneXp, entry.grant.petCoins),
                )
                val petId = entry.petId
                if (petId != null && entry.grant.petBondXp > 0) {
                    val existing = db.query(
                        "pet_progress",
                        arrayOf("bond_xp"),
                        "pet_id = ?",
                        arrayOf(petId),
                        null,
                        null,
                        null,
                        "1",
                    ).use { cursor ->
                        if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow("bond_xp")) else null
                    }
                    if (existing == null) {
                        db.insertOrThrow(
                            "pet_progress",
                            null,
                            ContentValues().apply {
                                put("pet_id", petId)
                                put("bond_xp", entry.grant.petBondXp)
                            },
                        )
                    } else {
                        db.update(
                            "pet_progress",
                            ContentValues().apply { put("bond_xp", existing + entry.grant.petBondXp) },
                            "pet_id = ?",
                            arrayOf(petId),
                        )
                    }
                }
                db.setTransactionSuccessful()
                true
            }
        } finally {
            db.endTransaction()
        }
    }

    override fun recent(limit: Int): List<RewardLedgerEntry> {
        if (limit <= 0) return emptyList()
        val out = mutableListOf<RewardLedgerEntry>()
        helper.readableDatabase.query(
            "reward_events",
            arrayOf(
                "event_id",
                "event_type",
                "actor_key",
                "pet_id",
                "decision",
                "evelune_xp",
                "pet_bond_xp",
                "pet_coins",
                "created_at",
            ),
            null,
            null,
            null,
            null,
            "created_at DESC",
            limit.toString(),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                out += RewardLedgerEntry(
                    eventId = cursor.getString(cursor.getColumnIndexOrThrow("event_id")),
                    eventType = RewardEventType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("event_type"))),
                    actorKey = cursor.getString(cursor.getColumnIndexOrThrow("actor_key")),
                    petId = cursor.getString(cursor.getColumnIndexOrThrow("pet_id")),
                    decision = RewardDecision.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("decision"))),
                    grant = RewardGrant(
                        eveluneXp = cursor.getInt(cursor.getColumnIndexOrThrow("evelune_xp")),
                        petBondXp = cursor.getInt(cursor.getColumnIndexOrThrow("pet_bond_xp")),
                        petCoins = cursor.getInt(cursor.getColumnIndexOrThrow("pet_coins")),
                    ),
                    createdAtEpochMs = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                )
            }
        }
        return out
    }

    private class Db(context: Context) : SQLiteOpenHelper(context, "evelune_rewards.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE reward_wallet (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    evelune_xp INTEGER NOT NULL DEFAULT 0,
                    pet_coins INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            db.execSQL("INSERT INTO reward_wallet(id, evelune_xp, pet_coins) VALUES (1, 0, 0)")
            db.execSQL(
                """
                CREATE TABLE pet_progress (
                    pet_id TEXT PRIMARY KEY,
                    bond_xp INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE reward_events (
                    event_id TEXT PRIMARY KEY,
                    event_type TEXT NOT NULL,
                    actor_key TEXT,
                    pet_id TEXT,
                    decision TEXT NOT NULL,
                    evelune_xp INTEGER NOT NULL,
                    pet_bond_xp INTEGER NOT NULL,
                    pet_coins INTEGER NOT NULL,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX idx_reward_events_type_time ON reward_events(event_type, created_at DESC)")
            db.execSQL("CREATE INDEX idx_reward_events_actor_time ON reward_events(actor_key, created_at DESC)")
            db.execSQL("CREATE INDEX idx_reward_events_pet_time ON reward_events(pet_id, created_at DESC)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }
}
