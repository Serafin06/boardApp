package com.example.apprafal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Player::class, GameSession::class, GameSessionParticipant::class, GamePick::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameSessionDao(): GameSessionDao
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

