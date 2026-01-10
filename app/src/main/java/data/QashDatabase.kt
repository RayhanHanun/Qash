package com.example.qash_finalproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, Transaction::class, Category::class], version = 6, exportSchema = false)
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
                    .fallbackToDestructiveMigration() // Reset database karena struktur berubah
                    .addCallback(QashDatabaseCallback(context)) // Panggil Callback Seeding
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class QashDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Jalankan di Background Thread
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.qashDao())
                }
            }
        }

        suspend fun populateDatabase(dao: QashDao) {
            // Data Kategori Default
            val categories = listOf(
                Category(name = "Pulsa"),
                Category(name = "Listrik"),
                Category(name = "PDAM"),
                Category(name = "Internet"),
                Category(name = "Top Up"),
                Category(name = "Transfer"),
                Category(name = "Makan & Minum"),
                Category(name = "Belanja"),
                Category(name = "Lainnya")
            )
            // Masukkan ke Tabel Category
            dao.insertAllCategories(categories)
        }
    }
}