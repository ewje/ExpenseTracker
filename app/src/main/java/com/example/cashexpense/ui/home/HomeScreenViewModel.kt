package com.example.cashexpense.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.AppRepository
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.TransactionsWithAccountAndCategory
import com.example.cashexpense.ui.transaction.AccountDetails
import com.example.cashexpense.ui.transaction.TransactionType
import com.example.cashexpense.ui.transaction.toDoubleWithTwoDecimalPlaces
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val repository: AppRepository
): ViewModel() {

    var accountUiState by mutableStateOf(AccountUiState())
        private set

    var homeUiState by mutableStateOf(HomeUiState())
        private set

    private val _transactionsState: StateFlow<List<TransactionsWithAccountAndCategory>> = repository.getTransactionsWithAccountAndCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactionsState: StateFlow<List<TransactionsWithAccountAndCategory>> = _transactionsState

    private var _accountsState: StateFlow<List<Account>> = repository.getAllAccountsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val accountsState: StateFlow<List<Account>> = _accountsState

    private val _categoriesState: StateFlow<List<Category>> = repository.getAllCategoriesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState

    fun saveAccount() {
        if(validateInput()) {
            viewModelScope.launch {
                repository.insertAccount(accountUiState.toAccount())
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch{
            repository.deleteAccount(account)
            repository.deleteTransactionsByAccountId(account.id)
        }
    }

    fun updateAccountUiState(accountDetails: AccountDetails) {
        accountUiState =
            AccountUiState(accountDetails)
    }

    private fun validateInput(uiState: AccountDetails = accountUiState.accountDetails): Boolean {
        return with(uiState) {
            accountName.isNotBlank()
        }
    }

    fun updateHomeUiState(account: Account?) {
        homeUiState = homeUiState.copy(selectedAccount = account)
    }

}

data class AccountUiState(
    val accountDetails: AccountDetails = AccountDetails()
)

data class HomeUiState(
    val transactions: List<TransactionsWithAccountAndCategory> = emptyList(),
    val accountList: List<Account> = emptyList(),
    val selectedAccount: Account? = accountList.firstOrNull()
)

fun AccountUiState.toAccount() = Account(
    id = accountDetails.id,
    accountName = accountDetails.accountName,
    accountColor = accountDetails.color,
    income = accountDetails.income.toDoubleWithTwoDecimalPlaces(),
    expense = accountDetails.expense.toDoubleWithTwoDecimalPlaces(),
    accAmount = accountDetails.amount.toDoubleWithTwoDecimalPlaces()
)

fun getIncome(list: List<TransactionsWithAccountAndCategory>, account: Account?): Double {
    val newList = list.filter {it.account == account && it.transaction.type == TransactionType.INCOME}
    return newList.sumOf {it.transaction.transAmount}
}

fun getExpense(list: List<TransactionsWithAccountAndCategory>, account: Account?): Double {
    val newList = list.filter {it.account == account && it.transaction.type == TransactionType.EXPENSE}
    return newList.sumOf {it.transaction.transAmount}
}

