package com.example.cashexpense.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.AppRepository
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.TransactionsWithAccountAndCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

class ReportScreenViewModel(
    repository: AppRepository
): ViewModel() {

    private val _reportsUiState = MutableStateFlow(ReportsUiState())
    val reportsUiState: StateFlow<ReportsUiState> = _reportsUiState

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactionState: StateFlow<List<TransactionsWithAccountAndCategory>> =
        reportsUiState
            .map { it.selectedAccount }
            .filterNotNull()
            .flatMapLatest { account ->
                repository.getTransactionsWithAccountAndCategoryByAccount(account.id)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _accountsState: StateFlow<List<Account>> = repository.getAllAccountsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val accountsState: StateFlow<List<Account>> = _accountsState

    private val _categoriesState: StateFlow<List<Category>> = repository.getAllCategoriesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState

    fun updateSelectedAccount(account: Account) {
        _reportsUiState.update { it.copy(selectedAccount = account, selectedCategory = null) }
    }

    fun updateSelectedMonth(yearMonth: YearMonth) {
        _reportsUiState.update { it.copy(selectedYearMonth = yearMonth) }
    }

    fun updateSelectedCategory(category: Category?) {
        _reportsUiState.update { it.copy(selectedCategory = category) }
    }

}

data class ReportsUiState(
    val selectedAccount: Account? = null,
    val selectedCategory: Category? = null,
    val selectedYearMonth: YearMonth = YearMonth.from(LocalDate.now())
)

fun Map<Category, List<TransactionsWithAccountAndCategory>>.toPieDataList(): List<PieData> {
    return this.map { (category, transactions) ->
        val totalAmount = transactions.sumOf { it.transaction.transAmount }

        PieData(
            name = category.categoryName,
            color = category.color,
            amount = totalAmount,
        )
    }.filter { it.amount > 0 } // Optional: exclude categories with 0 amount
}

data class PieData(
    val name: String,
    val color: Long,
    val amount: Double
)