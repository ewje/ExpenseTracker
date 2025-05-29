package com.example.cashexpense.ui.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.AppRepository
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.Transaction
import com.example.cashexpense.ui.settings.CategoryDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
                if(transactionUiState.transactionDetails.type != TransactionType.TRANSFER) {
                    repository.insertTransaction(transactionUiState.transactionDetails.toTransaction())
                    editAccountValue()
                } else if (transactionUiState.transactionDetails.type == TransactionType.TRANSFER) {
                    val transferOut = transactionUiState.transactionDetails.toTransfer()
                    val transferIn = transactionUiState.transactionDetails.toTransferIn(accounts = accountsState.value)
                    repository.insertTransaction(transferIn)
                    repository.insertTransaction(transferOut)
                    editAccountValue()
                }
            }
        }
    }

    fun isButtonEnabled(): Boolean {
        return validateInput()
    }

    private fun validateInput(uiState: TransactionDetails = transactionUiState.transactionDetails): Boolean {
        return if(uiState.type == TransactionType.TRANSFER){
            with(uiState) {
                amount.isNotBlank() && account.isNotBlank() && destinationAccount.isNotBlank()
            }
        } else {
            with(uiState) {
                title.isNotBlank() && (amount.isNotBlank()) && account.isNotBlank() && category.isNotBlank()
            }
        }
    }

    private fun editAccountValue() {
        val accounts = accountsState.value
        var account = accounts.find { it.accountName.trim() == transactionUiState.transactionDetails.account.trim() }
        var destinationAccount = accounts.find { it.accountName.trim() == transactionUiState.transactionDetails.destinationAccount.trim() }
        if (account != null) {
            when (transactionUiState.transactionDetails.type) {
                TransactionType.EXPENSE -> {
                    account = account.copy(
                        accAmount = (account.accAmount - transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()).toTwoDecimalPlaces(),
                        //expense = (account.expense + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces())
                        )
                }
                TransactionType.INCOME -> {
                    account = account.copy(
                        accAmount = (account.accAmount + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces()),
                        //income = (account.income + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces())
                    )
                }
                TransactionType.TRANSFER -> {
                    if(account != destinationAccount) {
                        account = account.copy(
                            accAmount = (account.accAmount - transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces())
                        )
                        if(destinationAccount != null) {
                            destinationAccount = destinationAccount.copy(
                                accAmount = (destinationAccount.accAmount + transactionUiState.transactionDetails.amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces())
                            )
                        }
                    }
                }
                TransactionType.TRANSFERIN -> {

                }
            }
            viewModelScope.launch {
                repository.updateAccount(account)
                if(transactionUiState.transactionDetails.type == TransactionType.TRANSFER && destinationAccount != null) {
                    repository.updateAccount(destinationAccount)
                }
            }
        }
    }
}

data class TransactionUiState(
    val transactionDetails: TransactionDetails = TransactionDetails(),
    val isEntryValid: Boolean = false
)

data class TransactionDetails(
    val id: Int = 0,
    val title: String = "",
    val amount: String = "",
    val date: Long = System.currentTimeMillis(),
    val details: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val account: String = "",
    val categoryId: Int = 0,
    val accountId: Int = 0,
    val destinationAccount: String = ""
)

data class AccountDetails(
    val id: Int = 0,
    val accountName: String = "",
    val color: Long = 0xFFFFFFFF,
    val income: String = "",
    val expense: String = "",
    val amount: String = ""
)
/*
data class CategoryDetails(
    val categoryName: String = "",
    val color: Long = 0
)

 */

enum class TransactionType(val label: String) {
    INCOME("Income"),
    EXPENSE("Expense"),
    TRANSFER("Outgoing Transfer"),
    TRANSFERIN("Incoming Transfer")
}

fun Transaction.toTransactionDetails(): TransactionDetails = TransactionDetails(
    id = id,
    title = title,
    amount = transAmount.toString(),
    date = date,
    details = details,
    type = type,
    categoryId = categoryIdFk,
    accountId = accountIdFk,
)

fun Account.toAccountDetails(): AccountDetails = AccountDetails(
    id = id,
    accountName = accountName,
    color = accountColor,
    amount = accAmount.toString(),
    expense = expense.toString(),
    income = income.toString()
)

fun AccountDetails.toAccount(): Account = Account(
    id = id,
    accountName = accountName,
    accountColor = color,
    accAmount = amount.toDoubleWithTwoDecimalPlaces(),
    income = income.toDoubleWithTwoDecimalPlaces(),
    expense = expense.toDoubleWithTwoDecimalPlaces()
)

fun Category.toCategoryDetails(): CategoryDetails = CategoryDetails(
    id = id,
    name = categoryName,
    color = color
)

fun TransactionDetails.toTransfer(): Transaction {
    return Transaction(
        id = id,
        title = "$account Transfer Out",
        transAmount = amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces(),
        date = date,
        details = "Transferred Balance: $account to $destinationAccount",
        type = type,
        accountIdFk = accountId,
        categoryIdFk = 100001
    )
}

fun TransactionDetails.toTransferIn(accounts: List<Account>): Transaction {
    return Transaction(
        id = id,
        title = "$destinationAccount Transfer In",
        transAmount = amount.removeRange(0,1).toDoubleWithTwoDecimalPlaces(),
        date = date,
        details = "Transferred Balance: $account to $destinationAccount",
        type = TransactionType.TRANSFERIN,
        accountIdFk = accounts.find {it.accountName == destinationAccount}?.id?:0,
        categoryIdFk = 100001
    )
}

fun TransactionDetails.toTransaction(): Transaction {
    return Transaction(
        id = id,
        title = title,
        transAmount = amount.removeRange(0, 1).toDoubleWithTwoDecimalPlaces(),
        date = date,
        details = details,
        type = type,
        accountIdFk = accountId,
        categoryIdFk = categoryId
    )
}

fun String.toDoubleWithTwoDecimalPlaces(): Double {
    return try {
        val formatted = "%.2f".format(this.toDouble())
        formatted.toDouble()
    } catch (e: NumberFormatException) {
        0.0 // Return default value if the string is not a valid number
    }
}

fun Double.toTwoDecimalPlaces(): Double {
    return "%.2f".format(this).toDouble()
}