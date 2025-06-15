package com.example.cashexpense.ui.transaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
    val isInitialized = viewModel.isInitialized
    if (!isInitialized && accounts.isNotEmpty() && categories.isNotEmpty()) {
        // Show loading or do nothing yet
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
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
}