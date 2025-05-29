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
    private lateinit var transferInTransaction: TransactionDetails

    private val _categoriesState: StateFlow<List<Category>> = repository.getAllCategoriesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState

    private val _accountsState: StateFlow<List<Account>> = repository.getAllAccountsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val accountsState: StateFlow<List<Account>> = _accountsState

    val transactionId1: Int = checkNotNull(savedStateHandle[TransactionEditDestination.transactionId1])

    val transactionId2 = savedStateHandle.get<Int>(TransactionEditDestination.transactionId2)?.takeIf { it != -1 }

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

            if(transactionId2 != null) {
                transferInTransaction = repository.getTransactionStream(transactionId2).filterNotNull().first().toTransactionDetails()
                transferInTransaction = transferInTransaction.copy(
                    account = accountsState.value.find { it.id == transferInTransaction.accountId }?.accountName ?: "",
                    category = categoriesState.value.find { it.id == transaction.categoryId }?.categoryName ?: "",
                    amount = "$${transaction.amount}"
                )
                transactionUiState = transactionUiState.copy(transactionDetails = transactionUiState.transactionDetails.copy(destinationAccount = transferInTransaction.account))
                initialTransaction = initialTransaction.copy(destinationAccount = transferInTransaction.account)
            } else {
                transferInTransaction = TransactionDetails()
            }
        }
    }

    private fun validateInput(uiState: TransactionDetails = transactionUiState.transactionDetails): Boolean {
        //return with(uiState) {
        //title.isNotBlank() && (amount.isNotBlank()) && account.isNotBlank() && category.isNotBlank()
        //}
        if(uiState.type == TransactionType.TRANSFER){
            return with(uiState) {
                amount.isNotBlank() && account.isNotBlank() && destinationAccount.isNotBlank()
            }
        } else {
            return with(uiState) {
                title.isNotBlank() && (amount.isNotBlank()) && account.isNotBlank() && category.isNotBlank()
            }
        }
    }

    fun isButtonEnabled(): Boolean {
        return validateInput()
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
                } else {
                    repository.updateTransaction(transactionUiState.transactionDetails.toTransfer())
                    transferInTransaction = transferInTransaction.copy(
                        amount = transactionUiState.transactionDetails.amount,
                        date = transactionUiState.transactionDetails.date,
                        account = transactionUiState.transactionDetails.destinationAccount,
                        destinationAccount = ""
                    )
                    repository.updateTransaction(transactionUiState.transactionDetails.copy(id = transferInTransaction.id).toTransferIn(accountsState.value))
                }
                editAccountValue()
            }
        }
    }

    private fun editAccountValue() {
        val accounts = accountsState.value

        val account = accounts.find { it.accountName.trim() == transactionUiState.transactionDetails.account.trim() }
        val destinationAccount = accounts.find{ it.accountName.trim() == transactionUiState.transactionDetails.destinationAccount.trim() }

        val initialAccount = accounts.find { it.accountName == initialTransaction.account }
        val initialDestination = accounts.find { it.accountName == initialTransaction.destinationAccount }

        val newAmount = transactionUiState.transactionDetails.amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces()
        val oldAmount = initialTransaction.amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces()

        val updatedAccounts = mutableMapOf<Int, Account>()

        if (account != null && initialAccount != null) {
            when (transactionUiState.transactionDetails.type) {
                TransactionType.EXPENSE -> {
                    if(initialTransaction.type == TransactionType.INCOME) {
                        updatedAccounts[initialAccount.id] = updatedAccounts[initialAccount.id]?.let {
                            it.copy(accAmount = it.accAmount - oldAmount)
                        } ?: initialAccount.copy(accAmount = initialAccount.accAmount - oldAmount)
                    } else {
                        updatedAccounts[initialAccount.id] = updatedAccounts[initialAccount.id]?.let {
                            it.copy(accAmount = it.accAmount + oldAmount)
                        } ?: initialAccount.copy(accAmount = initialAccount.accAmount + oldAmount)
                    }
                    updatedAccounts[account.id] = updatedAccounts[account.id]?.let {
                        it.copy(accAmount = it.accAmount - newAmount)
                    } ?: account.copy(accAmount = account.accAmount - newAmount)
                    /*
                    if (initialAccount == account) {
                        account = account.copy(
                            accAmount = (account.accAmount - newAmount + oldAmount).toTwoDecimalPlaces()
                        )
                        same = true
                    } else {
                        account = account.copy(accAmount = (account.accAmount - newAmount).toTwoDecimalPlaces())
                        initialAccount = initialAccount.copy(accAmount = (initialAccount.accAmount + oldAmount).toTwoDecimalPlaces())
                    }

                    updatedAccounts[account.id] = account
                    if (!same) updatedAccounts[initialAccount.id] = initialAccount

                     */
                }
                TransactionType.INCOME -> {
                    if(initialTransaction.type == TransactionType.INCOME) {
                        updatedAccounts[initialAccount.id] = updatedAccounts[initialAccount.id]?.let {
                            it.copy(accAmount = it.accAmount - oldAmount)
                        } ?: initialAccount.copy(accAmount = initialAccount.accAmount - oldAmount)
                    } else {
                        updatedAccounts[initialAccount.id] = updatedAccounts[initialAccount.id]?.let {
                            it.copy(accAmount = it.accAmount + oldAmount)
                        } ?: initialAccount.copy(accAmount = initialAccount.accAmount + oldAmount)
                    }
                    updatedAccounts[account.id] = updatedAccounts[account.id]?.let {
                        it.copy(accAmount = it.accAmount + newAmount)
                    } ?: account.copy(accAmount = account.accAmount + newAmount)
                }
                TransactionType.TRANSFER -> {
                    if (destinationAccount != null && initialDestination != null) {
                        // Copy to avoid mutating shared references
                        updatedAccounts[initialAccount.id] = updatedAccounts[initialAccount.id]?.let {
                            it.copy(accAmount = it.accAmount + oldAmount)
                        } ?: initialAccount.copy(accAmount = initialAccount.accAmount + oldAmount)

                        updatedAccounts[initialDestination.id] = updatedAccounts[initialDestination.id]?.let {
                            it.copy(accAmount = it.accAmount - oldAmount)
                        } ?: initialDestination.copy(accAmount = initialDestination.accAmount - oldAmount)

                        // Apply new transfer
                        updatedAccounts[account.id] = updatedAccounts[account.id]?.let {
                            it.copy(accAmount = it.accAmount - newAmount)
                        } ?: account.copy(accAmount = account.accAmount - newAmount)

                        updatedAccounts[destinationAccount.id] = updatedAccounts[destinationAccount.id]?.let {
                            it.copy(accAmount = it.accAmount + newAmount)
                        } ?: destinationAccount.copy(accAmount = destinationAccount.accAmount + newAmount)
                    }
                }
                TransactionType.TRANSFERIN -> {
                }
            }
            viewModelScope.launch {
                updatedAccounts.values.forEach { updated ->
                    repository.updateAccount(updated.copy(accAmount = updated.accAmount.toTwoDecimalPlaces()))
                }
            }
        }
    }
}

