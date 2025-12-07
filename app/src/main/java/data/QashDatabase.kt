package com.example.qash_finalproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Transaction::class], version = 1, exportSchema = false)
abstract class QashDatabase : RoomDatabase() {

    abstract fun qashDao(): QashDao

    companion object {
        @Volatile
        private var INSTANCE: QashDatabase? = null

        fun getDatabase(context: Context): QashDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QashDatabase::class.java,
                    "qash_database"
                )
                    // Callback opsional bisa ditambah di sini nanti kalau mau isi data awal
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}