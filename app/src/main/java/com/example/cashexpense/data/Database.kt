package com.example.cashexpense.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Category::class,
        Transaction::class,
        Account::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .addCallback(DatabaseCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }
        fun resetDatabase(context: Context) {
            val db = getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                db.clearAllTables()

                db.categoryDao().insertCategory(
                    Category(categoryName = "Transfer", color = 0xFF939393, id = 100001)
                )
            }
        }
    }
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Run on a background thread
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.categoryDao()?.insertCategory(
                    Category(categoryName = "Transfer", color = 0xFF939393, id = 100001)
                )
                // Add more inserts as needed
            }
        }
    }
}
