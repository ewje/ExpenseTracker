package com.example.cashexpense

import android.content.Context
import com.example.cashexpense.data.AppDatabase
import com.example.cashexpense.data.OfflineRepository

/*
object Graph {
    lateinit var db: AppDatabase
        private set

    val repository by lazy {
        OfflineRepository(
            transactionDao = db.transactionDao(),
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao()
        )
    }

    fun provide(context: Context) {
        db = AppDatabase.getDatabase(context)
    }
}

 */