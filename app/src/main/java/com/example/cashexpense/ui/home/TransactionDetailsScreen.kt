package com.example.cashexpense.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashexpense.NavDestination
import com.example.cashexpense.NavigationBar
import com.example.cashexpense.R
import com.example.cashexpense.data.formatDate
import com.example.cashexpense.ui.AppViewModelProvider
import com.example.cashexpense.ui.settings.CategoryDetails
import com.example.cashexpense.ui.transaction.AccountDetails
import com.example.cashexpense.ui.transaction.TransactionDetails
import java.time.Instant
import java.time.ZoneId

object TransactionDetailsDestination: NavDestination {
    const val routeWithoutArgs = "transaction_details"
    override val title = "Transaction Details"
    const val transactionIdArg = "transactionId"
    override val route = "$routeWithoutArgs/{$transactionIdArg}"
}


@Composable
fun TransactionDetailsScreen(
    viewModel: TransactionDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit
) {
    val transaction by viewModel.uiState.collectAsState()
    TransactionDetailsBody(
        modifier = Modifier,
        transactionDetailsUiState = transaction,
        onDelete = {
            viewModel.deleteTransaction()
            navigateBack()
        }
    )

}

@Composable
fun TransactionDetailsBody(
    modifier: Modifier,
    transactionDetailsUiState: TransactionDetailsUiState,
    onDelete: () -> Unit
) {
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
            Button(
                onClick = {},
                modifier = Modifier.weight(0.3f)
            ) {
                Text("Edit")
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(0.3f)
            ) {
                Text("Delete")
            }
        }

    }
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

    Card{
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