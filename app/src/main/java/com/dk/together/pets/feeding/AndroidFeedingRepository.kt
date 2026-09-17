package com.dk.together.pets.feeding

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AndroidFeedingRepository(context: Context) : FeedingRepository {
    private val helper = Db(context.applicationContext)

    override fun seedStarterInventoryIfEmpty(starter: Map<FoodId, Int>) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            db.rawQuery("SELECT COUNT(*) FROM food_inventory", null).use { cursor ->
                cursor.moveToFirst()
                if (cursor.getInt(0) == 0) {
                    starter.forEach { (food, amount) ->
                        require(amount >= 0)
                        if (amount > 0) {
                            db.insertOrThrow(
                                "food_inventory",
                                null,
                                ContentValues().apply {
                                    put("food_id", food.name)
                                    put("quantity", amount)
                                },
                            )
                        }
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun inventory(): Map<FoodId, Int> {
        val result = FoodId.entries.associateWith { 0 }.toMutableMap()
        helper.readableDatabase.query(
            "food_inventory",
            arrayOf("food_id", "quantity"),
            null,
            null,
            null,
            null,
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val food = runCatching { FoodId.valueOf(cursor.getString(0)) }.getOrNull() ?: continue
                result[food] = cursor.getInt(1)
            }
        }
        return result
    }

    override fun quantity(foodId: FoodId): Int {
        helper.readableDatabase.query(
            "food_inventory",
            arrayOf("quantity"),
            "food_id = ?",
            arrayOf(foodId.name),
            null,
            null,
            null,
            "1",
        ).use { cursor -> return if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
    }

    override fun add(foodId: FoodId, quantity: Int) {
        require(quantity > 0)
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val current = quantity(foodId)
            db.insertWithOnConflict(
                "food_inventory",
                null,
                ContentValues().apply {
                    put("food_id", foodId.name)
                    put("quantity", current + quantity)
                },
                SQLiteDatabase.CONFLICT_REPLACE,
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun receipt(interactionId: String): FeedingReceipt? {
        helper.readableDatabase.query(
            "feeding_receipts",
            RECEIPT_COLUMNS,
            "interaction_id = ?",
            arrayOf(interactionId),
            null,
            null,
            null,
            "1",
        ).use { cursor -> return if (cursor.moveToFirst()) cursor.toReceipt() else null }
    }

    override fun reserveFood(
        interactionId: String,
        petId: String,
        foodId: FoodId,
        nowEpochMs: Long,
    ): ReserveFoodResult {
        require(interactionId.isNotBlank())
        require(petId.isNotBlank())

        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            db.query(
                "feeding_receipts",
                RECEIPT_COLUMNS,
                "interaction_id = ?",
                arrayOf(interactionId),
                null,
                null,
                null,
                "1",
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val existing = cursor.toReceipt()
                    require(existing.petId == petId && existing.foodId == foodId) {
                        "Interaction id already belongs to a different feeding action"
                    }
                    db.setTransactionSuccessful()
                    return ReserveFoodResult(
                        decision = ReserveFoodDecision.ALREADY_RESERVED,
                        receipt = existing,
                        remainingQuantity = queryQuantity(db, foodId),
                    )
                }
            }

            val current = queryQuantity(db, foodId)
            if (current <= 0) {
                db.setTransactionSuccessful()
                return ReserveFoodResult(ReserveFoodDecision.OUT_OF_STOCK, null, 0)
            }

            db.insertWithOnConflict(
                "food_inventory",
                null,
                ContentValues().apply {
                    put("food_id", foodId.name)
                    put("quantity", current - 1)
                },
                SQLiteDatabase.CONFLICT_REPLACE,
            )

            val receipt = FeedingReceipt(
                interactionId = interactionId,
                petId = petId,
                foodId = foodId,
                status = FeedingReceiptStatus.RESERVED,
                startedAtEpochMs = nowEpochMs,
            )
            db.insertOrThrow("feeding_receipts", null, receipt.toValues())
            db.setTransactionSuccessful()
            return ReserveFoodResult(
                decision = ReserveFoodDecision.RESERVED,
                receipt = receipt,
                remainingQuantity = current - 1,
            )
        } finally {
            db.endTransaction()
        }
    }

    override fun markCompleted(interactionId: String, completedAtEpochMs: Long): FeedingReceipt {
        val existing = requireNotNull(receipt(interactionId)) { "Unknown feeding interaction: $interactionId" }
        if (existing.status == FeedingReceiptStatus.COMPLETED) return existing

        val completed = existing.copy(
            status = FeedingReceiptStatus.COMPLETED,
            completedAtEpochMs = completedAtEpochMs,
        )
        helper.writableDatabase.update(
            "feeding_receipts",
            completed.toValues(),
            "interaction_id = ?",
            arrayOf(interactionId),
        )
        return completed
    }

    private fun queryQuantity(db: SQLiteDatabase, foodId: FoodId): Int {
        db.query(
            "food_inventory",
            arrayOf("quantity"),
            "food_id = ?",
            arrayOf(foodId.name),
            null,
            null,
            null,
            "1",
        ).use { cursor -> return if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
    }

    private fun FeedingReceipt.toValues(): ContentValues = ContentValues().apply {
        put("interaction_id", interactionId)
        put("pet_id", petId)
        put("food_id", foodId.name)
        put("status", status.name)
        put("started_at", startedAtEpochMs)
        if (completedAtEpochMs == null) putNull("completed_at") else put("completed_at", completedAtEpochMs)
    }

    private fun android.database.Cursor.toReceipt(): FeedingReceipt {
        val completedIndex = getColumnIndexOrThrow("completed_at")
        return FeedingReceipt(
            interactionId = getString(getColumnIndexOrThrow("interaction_id")),
            petId = getString(getColumnIndexOrThrow("pet_id")),
            foodId = FoodId.valueOf(getString(getColumnIndexOrThrow("food_id"))),
            status = FeedingReceiptStatus.valueOf(getString(getColumnIndexOrThrow("status"))),
            startedAtEpochMs = getLong(getColumnIndexOrThrow("started_at")),
            completedAtEpochMs = if (isNull(completedIndex)) null else getLong(completedIndex),
        )
    }

    private class Db(context: Context) : SQLiteOpenHelper(context, "evelune_feeding.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE food_inventory (
                    food_id TEXT PRIMARY KEY,
                    quantity INTEGER NOT NULL CHECK(quantity >= 0)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE feeding_receipts (
                    interaction_id TEXT PRIMARY KEY,
                    pet_id TEXT NOT NULL,
                    food_id TEXT NOT NULL,
                    status TEXT NOT NULL,
                    started_at INTEGER NOT NULL,
                    completed_at INTEGER
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    private companion object {
        val RECEIPT_COLUMNS = arrayOf(
            "interaction_id",
            "pet_id",
            "food_id",
            "status",
            "started_at",
            "completed_at",
        )
    }
}
