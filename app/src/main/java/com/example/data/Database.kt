package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode: String, // "AI_MELD", "DUO_MELD", "SOLITAIRE", "PSYCHIC"
    val finalWord: String,
    val rounds: Int,
    val wordsTimeline: String, // JSON-like string or descriptive list
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean
)

@Dao
interface GameRecordDao {
    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<GameRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GameRecord)

    @Query("DELETE FROM game_records")
    suspend fun clearAll()
}

@Database(entities = [GameRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mind_meld_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class GameRepository(private val gameRecordDao: GameRecordDao) {
    val allRecords: Flow<List<GameRecord>> = gameRecordDao.getAllRecords()

    suspend fun saveRecord(record: GameRecord) {
        gameRecordDao.insertRecord(record)
    }

    suspend fun clearRecords() {
        gameRecordDao.clearAll()
    }
}
