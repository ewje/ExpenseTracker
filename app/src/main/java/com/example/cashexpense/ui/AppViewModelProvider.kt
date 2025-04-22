package com.example.cashexpense.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cashexpense.CashApplication
import com.example.cashexpense.ui.home.HomeScreenViewModel
import com.example.cashexpense.ui.home.TransactionDetailsViewModel
import com.example.cashexpense.ui.reports.ReportScreenViewModel
import com.example.cashexpense.ui.settings.CategoriesScreenViewModel
import com.example.cashexpense.ui.transaction.TransactionEditViewModel
import com.example.cashexpense.ui.transaction.TransactionEntryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeScreenViewModel(
                cashApplication().container.repository
            )
        }

        initializer {
            CategoriesScreenViewModel(
                cashApplication().container.repository
            )
        }

        initializer {
            TransactionEntryViewModel(
                cashApplication().container.repository
            )
        }

        initializer {
            TransactionDetailsViewModel(
                this.createSavedStateHandle(),
                cashApplication().container.repository
            )
        }

        initializer {
            TransactionEditViewModel(
                this.createSavedStateHandle(),
                cashApplication().container.repository
            )
        }

        initializer {
            ReportScreenViewModel(
                cashApplication().container.repository
            )
        }
    }
}

fun CreationExtras.cashApplication(): CashApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as CashApplication)