package com.neon.peggame.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.neon.peggame.viewmodel.GameMode

/**
 * Converts the GameMode enum to/from a String for storage in Room.
 */
class Converters {
    @TypeConverter
    fun fromGameMode(value: GameMode): String {
        return value.name
    }

    @TypeConverter
    fun toGameMode(value: String): GameMode {
        return GameMode.valueOf(value)
    }
}

@Database(entities = [PegScoreEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
}
