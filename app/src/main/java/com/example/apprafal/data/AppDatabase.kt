package com.example.apprafal.data

import android.content.Context
import androidx.room.*

@Database(
    entities = [
        Player::class,
        GameSession::class,
        GameSessionParticipant::class,
        GamePick::class

    ],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun gameSessionParticipantDao(): GameSessionParticipantDao
    abstract fun gamePickDao(): GamePickDao


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
                    .fallbackToDestructiveMigration() // Usuwa starą bazę i tworzy nową
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}