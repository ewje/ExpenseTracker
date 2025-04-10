package com.example.cashexpense.ui.transaction

import android.icu.text.DecimalFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.AppRepository
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class TransactionEntryViewModel(
        private val repository: AppRepository
    ): ViewModel() {

    var transactionUiState by mutableStateOf(TransactionUiState())
        private set

    private val _categoriesState: StateFlow<List<Category>> = repository.getAllCategoriesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState

    private val _accountsState: StateFlow<List<Account>> = repository.getAllAccountsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val accountsState: StateFlow<List<Account>> = _accountsState



    fun updateUiState(transactionDetails: TransactionDetails) {
        transactionUiState =
            TransactionUiState(transactionDetails = transactionDetails, isEntryValid = validateInput(transactionDetails))
    }

    fun saveTransaction() {
        if (validateInput()) {
            viewModelScope.launch {
                repository.insertTransaction(transactionUiState.transactionDetails.toTransaction(categoriesState, accountsState))
                editAccountValue()
            }
        }
    }

    private fun validateInput(uiState: TransactionDetails = transactionUiState.transactionDetails): Boolean {
        return with(uiState) {
            title.isNotBlank() && (amount.removeRange(0, 1).toDoubleOrNull() != null) && account.isNotBlank() && category.isNotBlank()
        }
    }

    private fun editAccountValue() {
        val accounts = accountsState.value
        var account = accounts.find { it.accountName.trim() == transactionUiState.transactionDetails.account.trim() }
        if (account != null) {
            when (transactionUiState.transactionDetails.type) {
                TransactionType.EXPENSE -> {
                    account = account.copy(
                        accAmount = (account.accAmount - transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()),
                        expense = (account.expense + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces())
                        )
                    println(account.accAmount)
                }
                TransactionType.INCOME -> {
                    account = account.copy(
                        accAmount = (account.accAmount + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()),
                        income = (account.income + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces())
                    )
                }
                TransactionType.TRANSFER -> {
                    account = account.copy(
                        accAmount = (account.accAmount - transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces())
                    )
                }
            }
            viewModelScope.launch {
                repository.updateAccount(account)
            }
        }
    }

}

data class TransactionUiState(
    val transactionDetails: TransactionDetails = TransactionDetails(),
    val isEntryValid: Boolean = false
)

data class TransactionDetails(
    val title: String = "",
    val amount: String = "",
    val date: Long = System.currentTimeMillis(),
    val details: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val account: String = ""
)

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

fun Transaction.toTransactionDetails(): TransactionDetails = TransactionDetails(
    title = title,
    amount = transAmount.toString(),
    date = date,
    details = details,
    type = type
)

private fun TransactionDetails.toTransaction(
    categoriesState: StateFlow<List<Category>>,
    accountsState: StateFlow<List<Account>>
): Transaction {
    val categoryIdFk: Int
    val accountIdFk: Int

    // Collect data from StateFlows
    val categories = categoriesState.value
    val accounts = accountsState.value

    // Perform find on lists
    categoryIdFk = categories.find { it.categoryName.trim() == category.trim() }?.id ?: 0
    accountIdFk = accounts.find { it.accountName.trim() == account.trim() }?.id ?: 0

    return Transaction(
        title = title,
        transAmount = amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces(),
        date = date,
        details = details,
        type = type,
        accountIdFk = accountIdFk,
        categoryIdFk = categoryIdFk
    )
}

private fun String.toDoubleWithTwoDecimalPlaces(): Double {
    return try {
        val formatted = "%.2f".format(this.toDouble())
        formatted.toDouble()
    } catch (e: NumberFormatException) {
        0.0 // Return default value if the string is not a valid number
    }
}