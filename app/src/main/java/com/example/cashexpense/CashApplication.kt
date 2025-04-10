package com.example.cashexpense

import android.app.Application
import com.example.cashexpense.data.AppContainer
import com.example.cashexpense.data.DefaultAppContainer

class CashApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}