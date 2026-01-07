package com.example.qash_finalproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Naikkan version menjadi 5 karena ada perubahan struktur User dan Transaction
@Database(entities = [User::class, Transaction::class], version = 5, exportSchema = false)
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
                    .fallbackToDestructiveMigration() // Ini akan menghapus data lama dan membuat ulang tabel
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
