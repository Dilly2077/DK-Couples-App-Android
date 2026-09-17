package com.dk.together.pets.engine

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Durable local repository for the pet engine.
 *
 * This is intentionally not wired into the current UI yet, but it is ready for the later Pets
 * integration. Hatch timers therefore survive process death/reboots because absolute timestamps,
 * hatch history, and owned pets are stored on disk.
 */
class AndroidPetEngineRepository(context: Context) : PetEngineRepository {
    private val helper = Db(context.applicationContext)

    override fun loadHistory(): HatchHistory {
        helper.readableDatabase.query(
            "hatch_history",
            arrayOf(
                "total_claimed",
                "owned_species",
                "consecutive_duplicates",
                "since_legendary",
                "since_dragon"
            ),
            "id = 1",
            null,
            null,
            null,
            null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return HatchHistory()
            val owned = cursor.getString(1)
                .orEmpty()
                .split(',')
                .filter { it.isNotBlank() }
                .mapNotNull { value -> runCatching { PetSpecies.valueOf(value) }.getOrNull() }
                .toSet()
            return HatchHistory(
                totalClaimedHatches = cursor.getInt(0),
                ownedSpecies = owned,
                consecutiveDuplicateHatches = cursor.getInt(2),
                hatchesSinceLegendaryOrBetter = cursor.getInt(3),
                hatchesSinceDragon = cursor.getInt(4)
            )
        }
    }

    override fun saveHistory(history: HatchHistory) {
        helper.writableDatabase.insertWithOnConflict(
            "hatch_history",
            null,
            ContentValues().apply {
                put("id", 1)
                put("total_claimed", history.totalClaimedHatches)
                put("owned_species", history.ownedSpecies.joinToString(",") { it.name })
                put("consecutive_duplicates", history.consecutiveDuplicateHatches)
                put("since_legendary", history.hatchesSinceLegendaryOrBetter)
                put("since_dragon", history.hatchesSinceDragon)
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    override fun loadActiveHatch(): HatchSession? {
        helper.readableDatabase.query(
            "active_hatch",
            arrayOf("id", "species", "started_at", "ends_at", "claimed_at"),
            null,
            null,
            null,
            null,
            "started_at DESC",
            "1"
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val claimedIndex = cursor.getColumnIndexOrThrow("claimed_at")
            return HatchSession(
                id = cursor.getString(0),
                species = PetSpecies.valueOf(cursor.getString(1)),
                startedAtEpochMs = cursor.getLong(2),
                endsAtEpochMs = cursor.getLong(3),
                claimedAtEpochMs = if (cursor.isNull(claimedIndex)) null else cursor.getLong(claimedIndex)
            )
        }
    }

    override fun saveActiveHatch(session: HatchSession?) {
        helper.writableDatabase.beginTransaction()
        try {
            helper.writableDatabase.delete("active_hatch", null, null)
            if (session != null) {
                helper.writableDatabase.insertOrThrow(
                    "active_hatch",
                    null,
                    ContentValues().apply {
                        put("id", session.id)
                        put("species", session.species.name)
                        put("started_at", session.startedAtEpochMs)
                        put("ends_at", session.endsAtEpochMs)
                        if (session.claimedAtEpochMs == null) putNull("claimed_at") else put("claimed_at", session.claimedAtEpochMs)
                    }
                )
            }
            helper.writableDatabase.setTransactionSuccessful()
        } finally {
            helper.writableDatabase.endTransaction()
        }
    }

    override fun loadPets(): List<PetInstance> {
        val pets = mutableListOf<PetInstance>()
        helper.readableDatabase.query(
            "pets",
            PET_COLUMNS,
            null,
            null,
            null,
            null,
            "hatched_at ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) pets += cursor.toPet()
        }
        return pets
    }

    override fun savePet(pet: PetInstance) {
        helper.writableDatabase.insertWithOnConflict(
            "pets",
            null,
            pet.toValues(),
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    override fun findPet(id: String): PetInstance? {
        helper.readableDatabase.query(
            "pets",
            PET_COLUMNS,
            "id = ?",
            arrayOf(id),
            null,
            null,
            null,
            "1"
        ).use { cursor -> return if (cursor.moveToFirst()) cursor.toPet() else null }
    }

    private fun PetInstance.toValues(): ContentValues = ContentValues().apply {
        put("id", id)
        put("species", species.name)
        if (nickname == null) putNull("nickname") else put("nickname", nickname)
        put("hatched_at", hatchedAtEpochMs)
        put("room", room.name)
        put("position_x", position.x)
        put("position_y", position.y)
        put("facing", facing.name)
        put("behaviour", behaviour.name)
        put("hunger", hunger)
        put("dirtiness", dirtiness)
        put("last_needs_update", lastNeedsUpdateEpochMs)
    }

    private fun android.database.Cursor.toPet(): PetInstance = PetInstance(
        id = getString(getColumnIndexOrThrow("id")),
        species = PetSpecies.valueOf(getString(getColumnIndexOrThrow("species"))),
        nickname = getColumnIndexOrThrow("nickname").let { if (isNull(it)) null else getString(it) },
        hatchedAtEpochMs = getLong(getColumnIndexOrThrow("hatched_at")),
        room = PetRoom.valueOf(getString(getColumnIndexOrThrow("room"))),
        position = NormalizedPosition(
            x = getDouble(getColumnIndexOrThrow("position_x")),
            y = getDouble(getColumnIndexOrThrow("position_y"))
        ),
        facing = FacingDirection.valueOf(getString(getColumnIndexOrThrow("facing"))),
        behaviour = PetBehaviour.valueOf(getString(getColumnIndexOrThrow("behaviour"))),
        hunger = getDouble(getColumnIndexOrThrow("hunger")),
        dirtiness = getDouble(getColumnIndexOrThrow("dirtiness")),
        lastNeedsUpdateEpochMs = getLong(getColumnIndexOrThrow("last_needs_update"))
    )

    private class Db(context: Context) : SQLiteOpenHelper(context, "evelune_pets.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE hatch_history (
                    id INTEGER PRIMARY KEY,
                    total_claimed INTEGER NOT NULL DEFAULT 0,
                    owned_species TEXT NOT NULL DEFAULT '',
                    consecutive_duplicates INTEGER NOT NULL DEFAULT 0,
                    since_legendary INTEGER NOT NULL DEFAULT 0,
                    since_dragon INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE active_hatch (
                    id TEXT PRIMARY KEY,
                    species TEXT NOT NULL,
                    started_at INTEGER NOT NULL,
                    ends_at INTEGER NOT NULL,
                    claimed_at INTEGER
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE pets (
                    id TEXT PRIMARY KEY,
                    species TEXT NOT NULL,
                    nickname TEXT,
                    hatched_at INTEGER NOT NULL,
                    room TEXT NOT NULL,
                    position_x REAL NOT NULL,
                    position_y REAL NOT NULL,
                    facing TEXT NOT NULL,
                    behaviour TEXT NOT NULL,
                    hunger REAL NOT NULL,
                    dirtiness REAL NOT NULL,
                    last_needs_update INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "INSERT INTO hatch_history (id, total_claimed, owned_species, consecutive_duplicates, since_legendary, since_dragon) VALUES (1, 0, '', 0, 0, 0)"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    private companion object {
        val PET_COLUMNS = arrayOf(
            "id",
            "species",
            "nickname",
            "hatched_at",
            "room",
            "position_x",
            "position_y",
            "facing",
            "behaviour",
            "hunger",
            "dirtiness",
            "last_needs_update"
        )
    }
}
