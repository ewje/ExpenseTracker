package com.example.cashexpense.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashexpense.data.AppRepository
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.TransactionsWithAccountAndCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TransactionsViewModel(
    private val repository: AppRepository
): ViewModel() {

    private val _transactionsState: StateFlow<List<TransactionsWithAccountAndCategory>> = repository.getTransactionsWithAccountAndCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactionsState: StateFlow<List<TransactionsWithAccountAndCategory>> = _transactionsState

    private val _categoriesState: StateFlow<List<Category>> = repository.getAllCategoriesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState
}