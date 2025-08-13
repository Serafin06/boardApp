package com.example.apprafal.data

import androidx.room.*
import java.util.UUID



@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long, // timestamp
    val currentPickerId: Int? = null,
    val gameName: String? = null,
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
    indices = [Index("sessionId"), Index("playerId"), Index("queuePosition")]
)

data class GameSessionParticipant(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val playerId: Int,
    val isPresent: Boolean = true,
    val canPickInSession: Boolean = true,
    val queuePosition: Int,
    )

@Entity(
    tableName = "game_picks",
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
    indices = [Index("sessionId"), Index("playerId"), Index("timestamp")]
)
data class GamePick(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val playerId: Int, // ZMIEŃ z String na Int
    val gameName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val pickOrder: Int,

)

// Data class dla query z joinami
data class ParticipantWithName(
    val id: String,
    val sessionId: String,
    val playerId: Int,
    val isPresent: Boolean,
    val canPickInSession: Boolean,
    val queuePosition: Int,
    val lastPickTimestamp: Long?,
    val playerName: String
)

// Data class dla UI
data class GamePickWithPlayerName(
    val playerName: String,
    val gameName: String,
    val timestamp: Long,
    val originalPick: GamePick
)