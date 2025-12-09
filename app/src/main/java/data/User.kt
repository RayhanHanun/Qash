package com.example.qash_finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val balance: Long,
    val points: Long = 0 // <--- TAMBAHKAN INI (Default 0)
)