package com.example.cashexpense.ui.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.cashexpense.NavDestination
import com.example.cashexpense.R
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.Category
import com.example.cashexpense.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.Locale


object TransactionEntryDestination: NavDestination {
    override val route = "transaction_entry"
    override val title = "Transaction"
    const val navTitle = "Add"
    val selectedIcon = Icons.Filled.Add
    val unselectedIcon = Icons.Outlined.Add
}

@Composable
fun TransactionEntryScreen(
    navigateBack: () -> Unit,
    viewModel: TransactionEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
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
        buttonText = "Add Transaction",
        isButtonEnabled = viewModel.isButtonEnabled(),
        isEdit = false
    )
}

@Composable
fun TransactionEntryBody(
    modifier: Modifier,
    transactionUiState: TransactionUiState,
    onTransactionValueChange: (TransactionDetails) -> Unit,
    onSaveClick: () -> Unit,
    categories: List<Category>,
    accounts: List<Account>,
    buttonText: String,
    isButtonEnabled: Boolean,
    isEdit: Boolean
) {
    Column(
        modifier
            .padding(start = dimensionResource(R.dimen.padding_medium), end = dimensionResource(R.dimen.padding_medium), top = dimensionResource(R.dimen.padding_medium))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TransactionTypeButtons(
            transactionUiState.transactionDetails,
            onValueChange = onTransactionValueChange,
            isEdit = isEdit
        )

        AmountCard(
            transactionUiState.transactionDetails,
            onValueChange = onTransactionValueChange
        )
        Date(
            transactionUiState.transactionDetails,
            onValueChange = onTransactionValueChange
        )
        Accounts(
            accounts = accounts,
            transactionUiState.transactionDetails,
            onValueChange = onTransactionValueChange,
            isDestination = false
        )
        if(transactionUiState.transactionDetails.type != TransactionType.TRANSFER) {
            Categories(
                categories = categories,
                transactionUiState.transactionDetails,
                onValueChange = onTransactionValueChange
            )
            Details(
                transactionUiState.transactionDetails,
                onValueChange = onTransactionValueChange
            )
        } else {
            Text(text = "Transfer To")
            Accounts(
                accounts = accounts,
                transactionDetails = transactionUiState.transactionDetails,
                onValueChange = onTransactionValueChange,
                isDestination = true
            )
        }
        Button(
            onClick = {
                onSaveClick()
            },
            enabled = isButtonEnabled
        ) {
            Text(buttonText)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Accounts(
    accounts: List<Account>,
    transactionDetails: TransactionDetails,
    onValueChange: (TransactionDetails) -> Unit = {},
    isDestination: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            //horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
        ) {
            items(accounts) {account ->
                if((isDestination && account.accountName != transactionDetails.account) || !isDestination){
                    Row{
                        OutlinedButton(
                            onClick = {
                                if(isDestination) {
                                    onValueChange(
                                        transactionDetails.copy(
                                            destinationAccount = account.accountName
                                        )
                                    )
                                } else {
                                    onValueChange(
                                        transactionDetails.copy(
                                            account = account.accountName,
                                            accountId = account.id
                                        )
                                    )
                                }
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if((!isDestination && account.accountName == transactionDetails.account) || (isDestination && account.accountName == transactionDetails.destinationAccount) ) Color(account.accountColor) else Color.LightGray
                            ),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 36.dp),
                            contentPadding = PaddingValues(dimensionResource(R.dimen.padding_small))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                            ){
                                RadioButton(
                                    selected = if (!isDestination) {(transactionDetails.account == account.accountName)} else {(transactionDetails.destinationAccount == account.accountName)},
                                    onClick = null,
                                    colors = RadioButtonColors(
                                        selectedColor = Color(account.accountColor),
                                        unselectedColor = Color(account.accountColor),
                                        disabledSelectedColor = Color(account.accountColor),
                                        disabledUnselectedColor = Color(account.accountColor)
                                    )
                                )
                                //Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
                                Text(text = account.accountName)
                                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
                            }
                        }
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionTypeButtons(
    transactionDetails: TransactionDetails,
    onValueChange: (TransactionDetails) -> Unit = {},
    isEdit: Boolean
) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            var type = emptyList<TransactionType>()
            if(!isEdit) {
                type = listOf(
                    TransactionType.EXPENSE,
                    TransactionType.INCOME,
                    TransactionType.TRANSFER
                )
            } else if (isEdit && transactionDetails.type != TransactionType.TRANSFER) {
                type = listOf(
                    TransactionType.EXPENSE,
                    TransactionType.INCOME
                )
            }

            type.forEach { item ->
                FilledTonalButton(
                    onClick = {
                        onValueChange(transactionDetails.copy(type = item))
                    },
                    colors = if(transactionDetails.type == item) {
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
                categories.filter {it.id != 100001}.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(text = category.categoryName) },
                        onClick = {
                            isDropDownExpanded.value = false
                            onValueChange(transactionDetails.copy(category = category.categoryName, categoryId = category.id))
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
            if(transactionDetails.type != TransactionType.TRANSFER) {
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
                HorizontalDivider(
                    thickness = 2.dp,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))
                )
            }
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

