package com.example.cashexpense.data

import android.content.Context

interface AppContainer {
    val repository: AppRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val repository:AppRepository by lazy {
        OfflineRepository(
            AppDatabase.getDatabase(context).transactionDao(),
            AppDatabase.getDatabase(context).accountDao(),
            AppDatabase.getDatabase(context).categoryDao()
        )
    }
}