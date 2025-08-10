package com.example.apprafal.data

import androidx.room.*
import java.util.UUID



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
    indices = [Index("sessionId"), Index("playerId"), Index("queuePosition")]
)
data class GameSessionParticipant(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val playerId: Int, // spójny typ Int
    val isPresent: Boolean = true,
    val queuePosition: Int,
    val weight: Float = 1.0f, // waga gracza dla algorytmu wyboru
    val isSkipped: Boolean = false, // czy został pominięty w tej rundzie
    val lastPickTimestamp: Long? = null // kiedy ostatnio wybierał
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
    val playerId: Int, // ZMIENIONE na Int dla spójności
    val gameName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val pickOrder: Int // który to był wybór w sesji (1, 2, 3...)
)

