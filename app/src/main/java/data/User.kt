package com.example.qash_finalproject.data // Sesuaikan package kamu

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // Kita cuma butuh 1 user, jadi ID-nya statis 1

    val name: String,
    val balance: Long = 0 // Saldo awal 0, pakai Long biar angkanya bisa besar
)