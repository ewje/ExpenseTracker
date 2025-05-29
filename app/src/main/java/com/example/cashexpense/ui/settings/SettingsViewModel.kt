package com.example.cashexpense.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashexpense.data.AppDatabase
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    fun eraseData(context: Context) {
        viewModelScope.launch {
            AppDatabase.resetDatabase(context)
        }
    }
}