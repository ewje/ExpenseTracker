package com.example.cashexpense.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashexpense.NavDestination
import com.example.cashexpense.R
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.MonthTransactions
import com.example.cashexpense.data.TransactionsWithAccountAndCategory
import com.example.cashexpense.data.groupByDay
import com.example.cashexpense.data.groupByMonth
import com.example.cashexpense.ui.AppViewModelProvider
import com.example.cashexpense.ui.home.TransactionItem
import com.example.cashexpense.ui.home.formatNumber
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object TransactionsDestination: NavDestination {
    override val route = "transactions"
    override val title = "Transactions"
    const val NAVTITLE = "All"
    //val selectedIcon = painterResource(R.drawable.payments_24px)
    //val unselectedIcon = Icon(painterResource(R.drawable.payments_24px))
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
    val transactionsByMonth = transactions.groupByMonth()
    if(transactionsByMonth.isNotEmpty()) {
        val listOfYearMonth =
            generateMonthRange(
                transactionsByMonth.keys.min().minusYears(1),
                transactionsByMonth.keys.max().plusYears(1)
            )
        val fullMonthMap: Map<YearMonth, MonthTransactions> = listOfYearMonth.associateWith { month ->
            transactionsByMonth[month] ?: MonthTransactions(yearMonth = month, income = 0.0, expense = 0.0, transactions = mutableListOf())
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
                modifier = Modifier.fillMaxWidth(),
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                listOfYearMonth.forEachIndexed { index, month ->
                    val selected = pagerState.currentPage == index
                    Tab(
                        selected = selected,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = month.format(DateTimeFormatter.ofPattern("MMMM")),
                                    color = if(selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                    style = MaterialTheme.typography.bodySmall.merge(TextStyle(lineHeight = 8.sp)),
                                    fontSize = if(month == YearMonth.now()) 16.sp else 12.sp,
                                    modifier = Modifier.padding(bottom = 0.dp),
                                    fontWeight = if(month == YearMonth.now()) FontWeight.Bold else FontWeight.Normal
                                )
                                if(month.year != LocalDate.now().year) {
                                    Text(
                                        text = month.format(DateTimeFormatter.ofPattern("yyyy")),
                                        fontSize = 8.sp,
                                        color = if(selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                        style = TextStyle(lineHeight = 2.sp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }





            // Horizontal pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,

                ) { page ->
                if (fullMonthMap[listOfYearMonth[page]]?.transactions?.isEmpty() != false) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                        Text(
                            text = "No transactions for this month",
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                } else {
                    val sortedTransactions = fullMonthMap[listOfYearMonth[page]]?.transactions?.groupByDay() ?: emptyMap()
                    val dateList = ArrayList(sortedTransactions.keys)
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
                    ) {
                        Text(
                            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                        IncomeExpense(
                            income = fullMonthMap[listOfYearMonth[page]]?.income ?: 0.0,
                            expense = fullMonthMap[listOfYearMonth[page]]?.expense ?: 0.0
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
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
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_extra_large)),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Add some Transactions!",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

}

@Composable
fun IncomeExpense(
    income: Double,
    expense: Double
){
    Card(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            //horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "",
                    tint = colorResource(R.color.income),
                    modifier = Modifier.graphicsLayer { scaleY = -1f }
                )
                Text(
                    text = "$${formatNumber(income)}",
                    color = colorResource(R.color.income)
                )
            }
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "",
                    tint = colorResource(R.color.expense)
                )
                Text(
                    text = "$${formatNumber(expense)}",
                    color = colorResource(R.color.expense)
                )
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

