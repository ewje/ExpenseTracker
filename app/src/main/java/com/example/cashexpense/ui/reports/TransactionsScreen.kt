package com.example.cashexpense.ui.reports

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashexpense.NavDestination
import com.example.cashexpense.R
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.MonthTransactions
import com.example.cashexpense.data.Transaction
import com.example.cashexpense.data.TransactionsWithAccountAndCategory
import com.example.cashexpense.data.groupByDay
import com.example.cashexpense.data.groupByMonth
import com.example.cashexpense.ui.AppViewModelProvider
import com.example.cashexpense.ui.home.TransactionItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object TransactionsDestination: NavDestination {
    override val route = "transactions"
    override val title = "Transactions"
    //val selectedIcon = //TODO
    //val unselectedIcon = //TODO
}

@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToTransactionDetails: (Int, Int) -> Unit
) {
    val transactions by viewModel.transactionsState.collectAsState()
    val categories by viewModel.categoriesState.collectAsState()

    TransactionsBody(
        transactions = transactions,
        categories = categories,
        navigateToTransactionDetails = navigateToTransactionDetails
    )
}

@Composable
private fun TransactionsBody(
    transactions: List<TransactionsWithAccountAndCategory>,
    categories: List<Category>,
    navigateToTransactionDetails: (Int, Int) -> Unit
) {
    val listOfYearMonth = generateMonthRange(YearMonth.of(2020, 1), YearMonth.of(2030, 1))
    val transactionsByMonth = transactions.groupByMonth()
    val fullMonthMap: Map<YearMonth, MonthTransactions> = listOfYearMonth.associateWith { month ->
        transactionsByMonth[month] ?: MonthTransactions(yearMonth = month, income = 0.0, expense = 0.0, transactions = mutableListOf<TransactionsWithAccountAndCategory>())
    }
    val pagerState = rememberPagerState(listOfYearMonth.indexOf(YearMonth.now())) {listOfYearMonth.size}
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(start = dimensionResource(R.dimen.padding_medium), end = dimensionResource(R.dimen.padding_medium), top = dimensionResource(R.dimen.padding_medium)),

    ) {
        // Month label
        val currentMonth = listOfYearMonth[pagerState.currentPage]
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOfYearMonth.forEachIndexed { index, month ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index)  } },
                    text = {
                        Text(
                            text = if(month.year == LocalDate.now().year) month.format(DateTimeFormatter.ofPattern("MMMM")) else month.format(DateTimeFormatter.ofPattern("MMMM yy")),
                            maxLines = 1
                        )
                    }
                )
            }
        }

        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Horizontal pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) { page ->
            if (fullMonthMap[listOfYearMonth[page]]?.transactions?.isEmpty() != false) {
                Text("No transactions for this month")
            } else {
                val sortedTransactions = fullMonthMap[listOfYearMonth[page]]?.transactions?.groupByDay() ?: emptyMap()
                val dateList = ArrayList(sortedTransactions.keys)
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
                ) {
                    dateList.forEach { date ->
                        TransactionItem(
                            transactions = sortedTransactions[date]!!,
                            date = date,
                            categories = categories,
                            onClick = navigateToTransactionDetails
                        )
                    }
                }
            }
        }
    }
}

fun generateMonthRange(from: YearMonth, to: YearMonth): List<YearMonth> {
    val months = mutableListOf<YearMonth>()
    var current = from
    while (!current.isAfter(to)) {
        months.add(current)
        current = current.plusMonths(1)
    }
    return months
}

