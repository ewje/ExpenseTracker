package com.example.cashexpense.ui.home


import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashexpense.R
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.DayTransactions
import com.example.cashexpense.data.Transaction
import com.example.cashexpense.data.TransactionsWithAccountAndCategory
import com.example.cashexpense.data.formatDate
import com.example.cashexpense.data.groupByDay
import com.example.cashexpense.ui.AppViewModelProvider
import com.example.cashexpense.ui.settings.ColorPicker
import com.example.cashexpense.ui.transaction.TransactionType
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    val transactions by viewModel.transactionsState.collectAsState()
    val accounts by viewModel.accountsState.collectAsState()
    val categories by viewModel.categoriesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                /*colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),

                 */
                title = {
                    Text(text = "Home",
                        style = MaterialTheme.typography.headlineLarge)
                }
            )
        }
    ) { innerPadding ->
        HomeBody(
            accounts = accounts,
            transactions = transactions,
            categories = categories,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            viewModel = viewModel
        )
    }
}

@Composable
private fun HomeBody(
    viewModel: HomeScreenViewModel,
    accounts: List<Account>,
    transactions: List<TransactionsWithAccountAndCategory>,
    categories: List<Category>,
    modifier: Modifier = Modifier
) {
    val selectedAcc: Account? = accounts.firstOrNull()
    val sortedTransactions = transactions.groupByDay()
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium))
    ) {

        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            items(accounts) {account ->


                AccountCard(
                    account,
                    selectedAccount = viewModel.homeUiState.selectedAccount ?: selectedAcc,
                    onClick =  viewModel::updateHomeUiState
                )
            }
            item {
                AddAccount(
                    onSaveClick = { viewModel.saveAccount() },
                    addAccountDetails = viewModel.addAccountUiState.addAccountDetails,
                    onAddAccountDetailsChange = viewModel::updateAddAccountUiState
                )
            }
        }
        AmountCard(selectedAccount = viewModel.homeUiState.selectedAccount ?: selectedAcc)

        Column() {
            Text(
                text = "TransactionList",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            LazyColumn() {
                itemsIndexed(
                    ArrayList(sortedTransactions.keys),
                    key = { _, date -> date}
                ) {_, date ->
                    TransactionItem(
                        transactions = sortedTransactions[date]!!,
                        date = date,
                        categories = categories,
                        onClick = {}
                    )

                }
            }
        }

    }
}

@Composable
private fun AccountCard(
    account: Account,
    modifier: Modifier = Modifier,
    selectedAccount: Account?,
    onClick: (Account) -> Unit
) {
    OutlinedCard(
        modifier = modifier.height(75.dp).clickable { onClick(account) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            2.dp,
            color =  if(account == selectedAccount) {
                Color(account.accountColor)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
                .fillMaxSize(),
        ) {
            Row() {
                Text(
                    text = account.accountName,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.padding(dimensionResource(id = R.dimen.padding_medium)))

                RadioButton(
                    selected = (selectedAccount == account),
                    onClick = null,
                    colors = RadioButtonColors(
                        selectedColor = Color(account.accountColor),
                        unselectedColor = Color(account.accountColor),
                        disabledSelectedColor = Color(account.accountColor),
                        disabledUnselectedColor = Color(account.accountColor)
                    )
                )
            }

            Text(
                text = account.accAmount.toString(),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun AddAccount(
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit,
    addAccountDetails: AddAccountDetails,
    onAddAccountDetailsChange: (AddAccountDetails) -> Unit
) {
    val openDialog = remember { mutableStateOf(false) }
    OutlinedCard(
        modifier = modifier.height(75.dp).clickable { openDialog.value = true },
           // .border(width = 4.dp, color = MaterialTheme.colorScheme.primaryContainer, shape = ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(4.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Add Account",
                color = MaterialTheme.colorScheme.secondary
            )
            Icon(
                Icons.Outlined.Add,
                contentDescription = "Add Account",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }

    if (openDialog.value) {
        AddAccountDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            saveAccount = onSaveClick,
            addAccountDetails = addAccountDetails,
            onAddAccountDetailsChange = onAddAccountDetailsChange
        )
    }
}

@Composable
private fun AddAccountDialog(
    onDismissRequest: () -> Unit,
    saveAccount: () -> Unit,
    addAccountDetails: AddAccountDetails,
    onAddAccountDetailsChange: (AddAccountDetails) -> Unit
) {
    val controller = rememberColorPickerController()
    val openDialog = remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                Text(
                    text = "Add Account",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(
                    thickness = 2.dp,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(width = 2.dp, color = Color(addAccountDetails.accountColor), shape = CircleShape)
                            .clickable { openDialog.value = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .align(Alignment.Center)
                                .background(Color(addAccountDetails.accountColor))
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)))
                    OutlinedTextField(
                        value = addAccountDetails.accountName,
                        onValueChange = {
                            onAddAccountDetailsChange(addAccountDetails.copy(accountName = it))
                        },
                        label = { Text("Account Name") },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                        )
                }

                OutlinedTextField(
                    value = addAccountDetails.startingValue,
                    onValueChange = {
                        onAddAccountDetailsChange(addAccountDetails.copy(startingValue = it))
                    },
                    label = { Text("Starting Value") },
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = {
                        onDismissRequest()
                        saveAccount()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text( "Confirm" )
                }
            }
        }
    }
    if (openDialog.value) {
        ColorPicker(
            controller = controller,
            onDismissRequest = {
                openDialog.value = false
            },
            saveColor = {
                onAddAccountDetailsChange(addAccountDetails.copy(accountColor = controller.selectedColor.value.toArgb().toLong()))
            },
            initialColor = Color(0xFFFFFFFF)
        )
    }
}

@Composable
private fun AmountCard(
    modifier: Modifier = Modifier,
    selectedAccount: Account?
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedAccount?.accountName ?: "Create an Account!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Clip
                    )
                }

                //Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.padding_extra_large)))
                Box(modifier = Modifier.wrapContentWidth(), contentAlignment = Alignment.TopEnd) {
                    Text(
                        text = "$${selectedAccount?.accAmount ?: 0.0}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
            Spacer(modifier = Modifier.padding(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column() {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                            text = "$${selectedAccount?.income ?: 0}",
                    style = MaterialTheme.typography.titleLarge
                    )
                }
                Column() {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$${selectedAccount?.expense ?: 0.0}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

            }
        }
    }
}

@Composable
private fun TransactionItem(
    transactions: DayTransactions,
    date: LocalDate,
    categories: List<Category>,
    onClick: () -> Unit
) {
    Column() {

        Text(
            text = date.formatDate(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
        HorizontalDivider(
                thickness = 2.dp,
        modifier = Modifier
            .padding(top = 4.dp,
                start = dimensionResource(R.dimen.padding_medium),
                end = dimensionResource(R.dimen.padding_medium))
        )
        transactions.transactions.forEach { transaction ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .clickable { onClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){

                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,

                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                color = Color(categories.find {
                                    transaction.transaction.categoryIdFk == it.id
                                }?.color ?: MaterialTheme.colorScheme.background.toArgb().toLong()),
                                shape = RoundedCornerShape(25.dp)
                            )
                            .padding(start = 4.dp, end = 4.dp, top = 1.dp)
                    ) {
                        Text(
                            text = categories.find { transaction.transaction.categoryIdFk == it.id }?.categoryName ?: "",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Box(
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = transaction.transaction.title
                        )
                    }
                }
                Text(
                    text = "$${transaction.transaction.transAmount}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (transaction.transaction.type == TransactionType.EXPENSE) {
                        Color(0xFFC23C3C)
                    } else {
                        Color(0xFF3EC738)
                    }

                )

            }
        }
    }
    Spacer(Modifier.padding(vertical = dimensionResource(R.dimen.padding_small)))
}



@Preview(showBackground = true)
@Composable
private fun HomeBodyPreview() {
    val list = mutableListOf(TransactionsWithAccountAndCategory(
        transaction = Transaction("lunch", 100.0, TransactionType.EXPENSE, 0, "", 0, 0, 0),
        account = Account("", 0.0, 0.0, 0.0, 0, 0),
        category = Category("", 0, 0)
    ))

    val categories: List<Category> = listOf(Category("food", 0x00ffff, 0))

   TransactionItem(
       transactions = DayTransactions(list, 0.0),
       date = LocalDate.now(),
       categories = categories,
       onClick = {}
   )
}