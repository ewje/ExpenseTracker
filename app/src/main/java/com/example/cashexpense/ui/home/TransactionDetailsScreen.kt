package com.example.cashexpense.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashexpense.NavDestination
import com.example.cashexpense.R
import com.example.cashexpense.data.formatDate
import com.example.cashexpense.ui.AppViewModelProvider
import com.example.cashexpense.ui.settings.CategoryDetails
import com.example.cashexpense.ui.transaction.AccountDetails
import com.example.cashexpense.ui.transaction.TransactionDetails
import com.example.cashexpense.ui.transaction.TransactionType
import java.time.Instant
import java.time.ZoneId

object TransactionDetailsDestination: NavDestination {
    const val routeWithoutArgs = "transaction_details"
    override val title = "Transaction Details"
    const val transactionId1 = "transactionId1"
    const val transactionId2 = "transactionId2"
    override val route = "$routeWithoutArgs/{$transactionId1}?${transactionId2}={${transactionId2}}"
}


@Composable
fun TransactionDetailsScreen(
    viewModel: TransactionDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit,
    navigateToTransactionEdit: (Int, Int) -> Unit
) {
    val transaction by viewModel.uiState.collectAsState()
    //println("${viewModel.transactionId2} and ${viewModel.transactionId1}")
    TransactionDetailsBody(
        modifier = Modifier,
        transactionDetailsUiState = transaction,
        onDelete = {
            viewModel.deleteTransaction()
            navigateBack()
        },
        navigateToTransactionEdit = navigateToTransactionEdit,
        viewModel = viewModel
    )

}

@Composable
fun TransactionDetailsBody(
    modifier: Modifier,
    transactionDetailsUiState: TransactionDetailsUiState,
    onDelete: () -> Unit,
    navigateToTransactionEdit: (Int, Int) -> Unit,
    viewModel: TransactionDetailsViewModel
) {
    val showDeleteDialog = remember { mutableStateOf(false)}
    Column(
        modifier = modifier.padding(dimensionResource(R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        TransactionDetailsCard(
            transactionDetails = transactionDetailsUiState.transactionDetails,
            accountDetails = transactionDetailsUiState.accountDetails,
            categoryDetails = transactionDetailsUiState.categoryDetails
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
        ) {
            if(transactionDetailsUiState.transactionDetails.type != TransactionType.TRANSFERIN){
                Button(
                    onClick = {
                        if(transactionDetailsUiState.transactionDetails.type == TransactionType.INCOME || transactionDetailsUiState.transactionDetails.type == TransactionType.EXPENSE) {
                            navigateToTransactionEdit(transactionDetailsUiState.transactionDetails.id, -1)
                        } else {
                            navigateToTransactionEdit(transactionDetailsUiState.transactionDetails.id, viewModel.transactionId2?: -1)
                            //println("${transactionDetailsUiState.transactionDetails.id}, ${viewModel.transactionId2}")
                        }
                        //navigateToTransactionEdit(transactionDetailsUiState.transactionDetails.id)
                    },
                    modifier = Modifier.weight(0.3f)
                ) {
                    Text("Edit")
                }
            } else {
                Button(
                    onClick = {},
                    enabled = false
                ) {
                    Text("Edit the original")
                }
            }

            OutlinedButton(
                onClick = {showDeleteDialog.value = true},
                modifier = Modifier.weight(0.3f)
            ) {
                Text("Delete")
            }
        }
    }
    DeleteConfirmationDialog(
        title = "Delete Transaction",
        message = "Are you sure you want to delete this transaction?",
        showDialog = showDeleteDialog.value,
        onConfirm = onDelete,
        onDismiss = { showDeleteDialog.value = false }
    )
}

@Composable
fun TransactionDetailsCard(
    transactionDetails: TransactionDetails,
    accountDetails: AccountDetails,
    categoryDetails: CategoryDetails
) {
    val date = Instant.ofEpochMilli(transactionDetails.date)           // Convert count-of-milliseconds-since-epoch into a date-time in UTC (`Instant`).
        .atZone(ZoneId.systemDefault())           // Adjust into the wall-clock time used by the people of a particular region (a time zone). Produces a `ZonedDateTime` object.
        .toLocalDate()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)//Color(selectedAccount?.accountColor?:MaterialTheme.colorScheme.surfaceVariant.toArgb().toLong()).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ){
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
        ) {
            Text(
                text = "${transactionDetails.type.label} on ${date.formatDate()}",
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider()
            Row {
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "Title:",
                        )
                        Text(
                            text = transactionDetails.title,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = Int.MAX_VALUE,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
                Box(modifier = Modifier.wrapContentWidth(), contentAlignment = Alignment.TopEnd) {
                    Column {
                        Text(
                            text = "Amount:",
                            modifier = Modifier.align(Alignment.End)
                        )
                        Text(
                            text = "$${transactionDetails.amount}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text("Account:")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    //modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ){
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(accountDetails.color))
                    )
                    Text(
                        text = accountDetails.accountName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text("Category:")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    //modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ){
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(categoryDetails.color))
                    )
                    Text(
                        text = categoryDetails.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Details:")
                Text(transactionDetails.details)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionDetailsPreview() {
   // TransactionDetailsScreen()
}