package com.example.cashexpense

import android.app.Application
import android.content.Context
import com.example.cashexpense.data.AppContainer
import com.example.cashexpense.data.DefaultAppContainer

class CashApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
/*
        if (isFirstLaunch(this)) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(this@CashApplication)

                db.categoryDao().insertCategory(
                    Category(categoryName = "Transfer", color = 0xFF939393, id = 100001)
                )
            }

            setFirstLaunchDone(this@CashApplication)
        }

 */
    }
}

fun isFirstLaunch(context: Context): Boolean {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("is_first_launch", true)
}

fun setFirstLaunchDone(context: Context) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("is_first_launch", false).apply()
}