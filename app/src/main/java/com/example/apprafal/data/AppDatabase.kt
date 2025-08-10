package com.example.apprafal.data

import android.content.Context
import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Player::class,
        GameSession::class,
        GameSessionParticipant::class,  // ZMIENIONE
        GamePick::class
        // USUŃ: GameQueueEntry::class
    ],
    version = 1, // ZWIĘKSZ wersję
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun gameSessionParticipantDao(): GameSessionParticipantDao  // NOWE
    abstract fun gamePickDao(): GamePickDao
    // USUŃ: abstract fun gameQueueDao(): GameQueueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Tymczasowo dla development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}





