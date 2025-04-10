package com.example.cashexpense.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.AppRepository
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.OfflineRepository
import com.example.cashexpense.data.Transaction
import com.example.cashexpense.data.TransactionsWithAccountAndCategory
import com.example.cashexpense.ui.settings.CategoryDetails
import com.example.cashexpense.ui.transaction.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.sql.Date
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HomeScreenViewModel(
    private val repository: AppRepository
): ViewModel() {

    var addAccountUiState by mutableStateOf(AddAccountUiState())
        private set

    var homeUiState by mutableStateOf(HomeUiState())
        private set

    private val _transactionsState: StateFlow<List<TransactionsWithAccountAndCategory>> = repository.getTransactionsWithAccountAndCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactionsState: StateFlow<List<TransactionsWithAccountAndCategory>> = _transactionsState

    private val _accountsState: StateFlow<List<Account>> = repository.getAllAccountsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val accountsState: StateFlow<List<Account>> = _accountsState

    private val _categoriesState: StateFlow<List<Category>> = repository.getAllCategoriesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState

    fun saveAccount() {
        if(validateInput()) {
            viewModelScope.launch {
                repository.insertAccount(addAccountUiState.toAccount())
            }
        }
    }

    fun updateAddAccountUiState(addAccountDetails: AddAccountDetails) {
        addAccountUiState =
            AddAccountUiState(addAccountDetails)
    }

    private fun validateInput(uiState: AddAccountDetails = addAccountUiState.addAccountDetails): Boolean {
        return with(uiState) {
            accountName.isNotBlank()
        }
    }

    private fun filterBy(accountId: Int) {
        viewModelScope.launch {
            repository.getTransactionsWithAccountAndCategoryByAccount(accountId).collectLatest {  }
        }
    }

    fun updateHomeUiState(account: Account) {
        homeUiState = homeUiState.copy(selectedAccount = account)
    }

}

data class AddAccountUiState(
    val addAccountDetails: AddAccountDetails = AddAccountDetails()
)

data class AddAccountDetails(
    val accountName: String = "",
    val startingValue: String = "",
    val accountColor: Long = 0xFFFFFFFF
)

data class HomeUiState(
    val transactions: List<TransactionsWithAccountAndCategory> = emptyList(),
    val accountList: List<Account> = emptyList(),
    val selectedAccount: Account? = accountList.firstOrNull()
)

fun AddAccountUiState.toAccount() = Account(
    accountName = addAccountDetails.accountName,
    accountColor = addAccountDetails.accountColor,
    income = 0.0,
    expense = 0.0,
    accAmount = addAccountDetails.startingValue.toDoubleOrNull() ?: 0.0
)



