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
    version = 2,
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
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "board_app_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}