package com.example.apprafal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val canChooseGame: Boolean = false,
    val queuePosition: Int? = null
)

// Główna tabela sesji gier
@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long, // timestamp
    val currentPickerId: Int? = null, // kto aktualnie wybiera
    val isCompleted: Boolean = false
)