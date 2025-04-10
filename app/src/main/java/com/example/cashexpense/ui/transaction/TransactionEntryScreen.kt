package com.example.cashexpense.ui.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashexpense.R
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.Category
import com.example.cashexpense.ui.AppViewModelProvider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntryScreen(
    navigateBack: () -> Unit,
    viewModel: TransactionEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val accounts by viewModel.accountsState.collectAsState()
    val categories by viewModel.categoriesState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transaction",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        TransactionEntryBody(
            modifier = Modifier.padding(innerPadding),
            transactionUiState = viewModel.transactionUiState,
            onTransactionValueChange = viewModel::updateUiState,
            onSaveClick = {
                viewModel.saveTransaction()
                navigateBack()
            },
            categories = categories,
            accounts = accounts
        )
    }
}

@Composable
private fun TransactionEntryBody(
    modifier: Modifier,
    transactionUiState: TransactionUiState,
    onTransactionValueChange: (TransactionDetails) -> Unit,
    onSaveClick: () -> Unit,
    categories: List<Category>,
    accounts: List<Account>
) {
    Column(
        modifier
            .padding(dimensionResource(id = R.dimen.padding_medium))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Accounts(
            accounts = accounts,
            transactionUiState.transactionDetails,
            onValueChange = onTransactionValueChange
        )
        TransactionTypeButtons(
            transactionUiState.transactionDetails,
            onValueChange = onTransactionValueChange
        )
        AmountCard(
            transactionUiState.transactionDetails,
            onValueChange = onTransactionValueChange
        )
        Date(
            transactionUiState.transactionDetails,
            onValueChange = onTransactionValueChange
        )
        Categories(
            categories = categories,
            transactionUiState.transactionDetails,
            onValueChange = onTransactionValueChange
        )
        Details(
            transactionUiState.transactionDetails,
            onValueChange = onTransactionValueChange
        )
        Button(onClick = {
            onSaveClick()
        }) {
            Text("Add Transaction")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Accounts(
    accounts: List<Account>,
    transactionDetails: TransactionDetails,
    onValueChange: (TransactionDetails) -> Unit = {},
) {
    val isDropDownExpanded = remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            isDropDownExpanded.value = true
        }
    ) {
        ExposedDropdownMenuBox(
            expanded = isDropDownExpanded.value,
            onExpandedChange = { isDropDownExpanded.value = !isDropDownExpanded.value },
            modifier = Modifier
        ) {
            TextField(
                readOnly = true,
                value = transactionDetails.account,
                onValueChange = {},
                label = { Text(text = "Account") },
                trailingIcon = {
                    TrailingIcon(expanded = isDropDownExpanded.value)
                },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )

            ExposedDropdownMenu(expanded = isDropDownExpanded.value, onDismissRequest = { isDropDownExpanded.value = false }) {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(text = account.accountName) },
                        onClick = {
                            isDropDownExpanded.value = false
                            onValueChange(transactionDetails.copy(account = account.accountName))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionTypeButtons(
    transactionDetails: TransactionDetails,
    onValueChange: (TransactionDetails) -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        var selectedTypeIndex by rememberSaveable {
            mutableStateOf(0)
        }
        val type = listOf(
            TransactionType.EXPENSE,
            TransactionType.INCOME,
            TransactionType.TRANSFER,
        )
        type.forEachIndexed { index, item ->
            FilledTonalButton(
                onClick = {
                    selectedTypeIndex = index
                    onValueChange(transactionDetails.copy(type = item))
                },
                colors = if(selectedTypeIndex == index) {
                    ButtonDefaults.buttonColors()
                } else ButtonDefaults.filledTonalButtonColors()
            ) {
                Text(text = item.name)
            }
        }
    }
}


@Composable
private fun AmountCard(
    transactionDetails: TransactionDetails,
    onValueChange: (TransactionDetails) -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.padding_medium)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Amount:")
            Box {
                TextField(
                    value = TextFieldValue(transactionDetails.amount, selection = TextRange(transactionDetails.amount.length)),
                    onValueChange = { newValue ->
                        val newValueWithDollar = if (newValue.text.isEmpty() || !newValue.text.startsWith("$")) {
                            "$${newValue.text}"
                        } else {
                            newValue.text
                        }
                        onValueChange(transactionDetails.copy(amount = newValueWithDollar))
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )

                if(transactionDetails.amount.isBlank()) {
                    Text(
                        text = "$0",
                        style = LocalTextStyle.current.copy(
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth().align(Alignment.CenterEnd)
                    )
                }

            }

        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Date(
    transactionDetails: TransactionDetails,
    onValueChange: (TransactionDetails) -> Unit = {},
) {
    val openDialog = remember { mutableStateOf(false)}
    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
        java.util.Date(
            transactionDetails.date
        )
    )
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { openDialog.value = true },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = { openDialog.value = true }
            ) {
                Icon(Icons.Outlined.DateRange, contentDescription = "Date")
            }
            Text(
                text = formattedDate,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
            )
        }

        if (openDialog.value) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = transactionDetails.date)
            val confirmEnabled = derivedStateOf { datePickerState.selectedDateMillis != null }

            DatePickerDialog(
                onDismissRequest = { openDialog.value = false },

                confirmButton = {
                    TextButton( onClick = {
                        openDialog.value = false
                        val date = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        onValueChange(transactionDetails.copy(date = date))
                    }, enabled = confirmEnabled.value
                    ) {
                        Text(text = "Confirm")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Categories(
    categories: List<Category>,
    transactionDetails: TransactionDetails,
    onValueChange: (TransactionDetails) -> Unit = {},
) {
    val isDropDownExpanded = remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            isDropDownExpanded.value = true
        }
    ) {
        ExposedDropdownMenuBox(
            expanded = isDropDownExpanded.value,
            onExpandedChange = { isDropDownExpanded.value = !isDropDownExpanded.value },
            modifier = Modifier
        ) {
            TextField(
                readOnly = true,
                value = transactionDetails.category,
                onValueChange = {},
                label = { Text(text = "Category") },
                trailingIcon = {
                    TrailingIcon(expanded = isDropDownExpanded.value)
                },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )

            ExposedDropdownMenu(expanded = isDropDownExpanded.value, onDismissRequest = { isDropDownExpanded.value = false }) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(text = category.categoryName) },
                        onClick = {
                            isDropDownExpanded.value = false
                            onValueChange(transactionDetails.copy(category = category.categoryName))
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun Details(
    transactionDetails: TransactionDetails,
    onValueChange: (TransactionDetails) -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            TextField(
                value = transactionDetails.title,
                onValueChange = { onValueChange(transactionDetails.copy(title = it)) },
                label = { Text("Title") },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
            HorizontalDivider(thickness = 2.dp,
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)))
            TextField(
                value = transactionDetails.details,
                onValueChange = { onValueChange(transactionDetails.copy(details = it)) },
                label = { Text("Details") },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                minLines = 5
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun TransactionEntryPreview() {
    //TransactionEntryScreen()
}

