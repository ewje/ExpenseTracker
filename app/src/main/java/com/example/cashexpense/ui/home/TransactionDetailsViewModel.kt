package com.example.cashexpense.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashexpense.data.AppRepository
import com.example.cashexpense.ui.transaction.AccountDetails
import com.example.cashexpense.ui.transaction.CategoryDetails
import com.example.cashexpense.ui.transaction.TransactionDetails
import com.example.cashexpense.ui.transaction.TransactionType
import com.example.cashexpense.ui.transaction.toAccount
import com.example.cashexpense.ui.transaction.toAccountDetails
import com.example.cashexpense.ui.transaction.toCategoryDetails
import com.example.cashexpense.ui.transaction.toDoubleWithTwoDecimalPlaces
import com.example.cashexpense.ui.transaction.toTransaction
import com.example.cashexpense.ui.transaction.toTransactionDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: AppRepository
): ViewModel() {
    private val transactionId: Int = checkNotNull(savedStateHandle[TransactionDetailsDestination.transactionIdArg])

    val uiState: StateFlow<TransactionDetailsUiState> =
        repository.getTransactionWithAccountAndCategory(transactionId)
            .filterNotNull()
            .map{
                TransactionDetailsUiState(
                    transactionDetails = it.transaction.toTransactionDetails(),
                    accountDetails = it.account.toAccountDetails(),
                    categoryDetails = it.category.toCategoryDetails())
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = TransactionDetailsUiState()
            )
    fun deleteTransaction() {
        viewModelScope.launch {
            println("DELETE")
            repository.deleteTransaction(uiState.value.transactionDetails.toTransaction())
            editAccountValue()
        }
    }
    private fun editAccountValue() {
        var account = uiState.value.accountDetails
        when (uiState.value.transactionDetails.type) {
            TransactionType.EXPENSE -> {
                account = account.copy(
                    amount = (account.amount.toDoubleWithTwoDecimalPlaces() + uiState.value.transactionDetails.amount.toDoubleWithTwoDecimalPlaces()).toString(),
                    expense = (account.expense.toDoubleWithTwoDecimalPlaces() - uiState.value.transactionDetails.amount.toDoubleWithTwoDecimalPlaces()).toString()
                )
            }
            TransactionType.INCOME -> {
                account = account.copy(
                    amount = (account.amount.toDoubleWithTwoDecimalPlaces() - uiState.value.transactionDetails.amount.toDoubleWithTwoDecimalPlaces()).toString(),
                    income = (account.income.toDoubleWithTwoDecimalPlaces() - uiState.value.transactionDetails.amount.toDoubleWithTwoDecimalPlaces()).toString()
                )
            }
            TransactionType.TRANSFER -> {
                account = account.copy(
                    amount = (account.amount + uiState.value.transactionDetails.amount.toDoubleWithTwoDecimalPlaces())
                )
            }
        }
        viewModelScope.launch {
            repository.updateAccount(account.toAccount())
        }
    }
}

data class TransactionDetailsUiState(
    val transactionDetails: TransactionDetails = TransactionDetails(),
    val accountDetails: AccountDetails = AccountDetails(),
    val categoryDetails: CategoryDetails = CategoryDetails()
)