package com.example.apprafal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey val id: String, // użyj UUID
    val name: String
)
