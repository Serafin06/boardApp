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


