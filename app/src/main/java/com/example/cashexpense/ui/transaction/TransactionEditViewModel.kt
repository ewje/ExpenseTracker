package com.example.cashexpense.ui.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.AppRepository
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: AppRepository
): ViewModel() {
    var transactionUiState by mutableStateOf(TransactionUiState())
        private set

    private lateinit var initialTransaction: TransactionDetails

    private val _categoriesState: StateFlow<List<Category>> = repository.getAllCategoriesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState

    private val _accountsState: StateFlow<List<Account>> = repository.getAllAccountsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val accountsState: StateFlow<List<Account>> = _accountsState

    private val transactionId1: Int = checkNotNull(savedStateHandle[TransactionEditDestination.transactionId1])

    //val initialTransaction: TransactionDetails = repository.getTransactionStream(transactionId).filterNotNull().first().toTransactionDetails()

    init {
        viewModelScope.launch {
            var transaction = repository.getTransactionStream(transactionId1).filterNotNull().first().toTransactionDetails()
            transaction = transaction.copy(
                account = accountsState.value.find { it.id == transaction.accountId }?.accountName ?: "",
                category = categoriesState.value.find { it.id == transaction.categoryId }?.categoryName ?: "",
                amount = "$${transaction.amount}")
            transactionUiState = TransactionUiState(transaction)
            initialTransaction = transaction
        }
    }

    private fun validateInput(uiState: TransactionDetails = transactionUiState.transactionDetails): Boolean {
        return with(uiState) {
            title.isNotBlank() && (amount.isNotBlank()) && account.isNotBlank() && category.isNotBlank()
        }
    }

    fun updateUiState(transactionDetails: TransactionDetails) {
        transactionUiState =
            TransactionUiState(transactionDetails = transactionDetails, isEntryValid = validateInput(transactionDetails))
    }

    fun saveTransaction() {
        if (validateInput()) {
            viewModelScope.launch {
                if(transactionUiState.transactionDetails.type != TransactionType.TRANSFER) {
                    repository.updateTransaction(transactionUiState.transactionDetails.toTransaction())

                }
                editAccountValue()
            }
        }
    }

    private fun editAccountValue() {
        val accounts = accountsState.value
        var account = accounts.find { it.accountName.trim() == transactionUiState.transactionDetails.account.trim() }
        var initialAccount = accounts.find { it.accountName == initialTransaction.account }
        val same: Boolean
        println("$account + $initialAccount + $initialTransaction")
        if (account != null && initialAccount != null) {
            when (transactionUiState.transactionDetails.type) {
                TransactionType.EXPENSE -> {
                    if(initialAccount == account) {
                        account = account.copy(
                            accAmount = (account.accAmount - transactionUiState.transactionDetails.amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces() + initialTransaction.amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces(),
                            //expense = (account.expense + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces() - initialTransaction.amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces()
                        )
                        same = true
                    } else {
                        account = account.copy(
                            accAmount = (account.accAmount - transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces(),
                            //expense = (account.expense + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces()
                        )
                        initialAccount = initialAccount.copy(
                            accAmount = (initialAccount.accAmount + initialTransaction.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces(),
                            //expense = (initialAccount.expense - initialTransaction.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces()
                        )
                        same = false
                    }
                }
                TransactionType.INCOME -> {
                    if(initialAccount == account) {
                        account = account.copy(
                            accAmount = (account.accAmount + transactionUiState.transactionDetails.amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces() - initialTransaction.amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces(),
                            //income = (account.expense + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces() - initialTransaction.amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces()
                        )
                        same = true
                    } else {
                        account = account.copy(
                            accAmount = (account.accAmount + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()),
                            //income = (account.income + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces())
                        )
                        initialAccount = initialAccount.copy(
                            accAmount = (initialAccount.accAmount - initialTransaction.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces(),
                            //income = (initialAccount.income - initialTransaction.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces()
                        )
                        same = false
                    }
                }

                TransactionType.TRANSFER -> {
                    if(initialAccount == account) {
                        account = account.copy(
                            accAmount = (account.accAmount - transactionUiState.transactionDetails.amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces() + initialTransaction.amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces()
                        )
                        same = true
                    } else {
                        account = account.copy(
                            accAmount = (account.accAmount - transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces())
                        )
                        initialAccount = initialAccount.copy(
                            accAmount = (initialAccount.accAmount + initialTransaction.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces())
                        )
                        same = false
                    }
                }

                TransactionType.TRANSFERIN -> {
                    same = true
                }
            }
            viewModelScope.launch {
                if(same) {
                    repository.updateAccount(account)
                } else {
                    repository.updateAccount(account)
                    repository.updateAccount(initialAccount)
                }
            }
        }
    }
}

