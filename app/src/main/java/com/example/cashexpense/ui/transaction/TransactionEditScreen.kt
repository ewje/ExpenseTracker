package com.example.cashexpense.ui.transaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashexpense.NavDestination
import com.example.cashexpense.ui.AppViewModelProvider


object TransactionEditDestination: NavDestination {
    const val routeWithoutArgs = "transaction_edit"
    override val title = "Edit Transaction"
    const val transactionId1 = "transactionEditId1"
    const val transactionId2 = "transactionEditId2"
    override val route = "$routeWithoutArgs/{$transactionId1}?${transactionId2}={${transactionId2}}"
}

@Composable
fun TransactionEditScreen(
    navigateBack: () -> Unit,
    viewModel: TransactionEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    val accounts by viewModel.accountsState.collectAsState()
    val categories by viewModel.categoriesState.collectAsState()
    TransactionEntryBody(
        modifier = Modifier,
        transactionUiState = viewModel.transactionUiState,
        onTransactionValueChange = viewModel::updateUiState,
        onSaveClick = {
            viewModel.saveTransaction()
            navigateBack()
        },
        categories = categories,
        accounts = accounts,
        buttonText = "Save Transaction",
        isButtonEnabled = viewModel.isButtonEnabled(),
        isEdit = true
    )
}