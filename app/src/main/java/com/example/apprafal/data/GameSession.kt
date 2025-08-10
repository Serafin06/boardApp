package com.example.apprafal.data

import androidx.room.*
import java.util.UUID


@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long // timestamp
)


@Entity(
    tableName = "session_participants",
    foreignKeys = [
        ForeignKey(
            entity = GameSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Player::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("playerId")]
)
data class GameSessionParticipant(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val playerId: Int,
    val isPresent: Boolean = true,
    val choseGame: Boolean = false,
    val queuePosition: Int,
    )

