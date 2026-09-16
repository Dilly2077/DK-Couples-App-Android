package com.dk.together.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity(tableName = "interactions")
data class InteractionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String,
    val actor: String,
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface InteractionDao {
    @Query("SELECT * FROM interactions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<InteractionEntity>>

    @Insert
    suspend fun insert(item: InteractionEntity)

    @Query("SELECT COUNT(*) FROM interactions WHERE type = :type")
    suspend fun countByType(type: String): Int
}

@Database(entities = [InteractionEntity::class], version = 1, exportSchema = false)
abstract class CoupleDatabase : RoomDatabase() {
    abstract fun interactionDao(): InteractionDao

    companion object {
        @Volatile private var instance: CoupleDatabase? = null

        fun get(context: Context): CoupleDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CoupleDatabase::class.java,
                "dk_together.db"
            ).build().also { instance = it }
        }
    }
}
