package com.example.cashexpense.ui.reports

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashexpense.NavDestination
import com.example.cashexpense.R
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.MonthTransactions
import com.example.cashexpense.data.TransactionsWithAccountAndCategory
import com.example.cashexpense.data.groupByDay
import com.example.cashexpense.data.groupByMonth
import com.example.cashexpense.ui.AppViewModelProvider
import com.example.cashexpense.ui.home.AccountCard
import com.example.cashexpense.ui.home.TransactionItem
import com.example.cashexpense.ui.transaction.TransactionType
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt


object ReportDestination: NavDestination {
    override val route = "reports"
    override val title = "Reports"
    val selectedIcon = Icons.Filled.ShoppingCart
    val unselectedIcon = Icons.Outlined.ShoppingCart
}

@Composable
fun ReportsScreen(
    viewModel: ReportScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToTransactionDetails: (Int, Int) -> Unit
) {
    val transactions by viewModel.transactionState.collectAsState()
    val categories by viewModel.categoriesState.collectAsState()
    val accounts by viewModel.accountsState.collectAsState()
    val uiState by viewModel.reportsUiState.collectAsState()
    if(accounts.isNotEmpty() && uiState.selectedAccount == null) {
        viewModel.updateSelectedAccount(accounts.first())
    }
    ReportsBody(
        viewModel = viewModel,
        transactions = transactions,
        category = categories,
        accounts = accounts,
        uiState = uiState,
        navigateToTransactionDetails = navigateToTransactionDetails
    )
}

@Composable
fun ReportsBody(
    viewModel: ReportScreenViewModel,
    transactions: List<TransactionsWithAccountAndCategory>,
    category: List<Category>,
    accounts: List<Account>,
    uiState: ReportsUiState,
    navigateToTransactionDetails: (Int, Int) -> Unit
) {
    val transactionsByType = transactions.groupBy { it.transaction.type }
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
        modifier = Modifier.padding(
            start = dimensionResource(R.dimen.padding_medium),
            end = dimensionResource(R.dimen.padding_medium),
            top = dimensionResource(R.dimen.padding_medium)
        ).verticalScroll(rememberScrollState())
    ) {
        if(accounts.isNotEmpty()) {
            LazyRow(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
            ) {
                items(accounts) { account ->
                    AccountCard(
                        account,
                        selectedAccount = uiState.selectedAccount,
                        onClick = viewModel::updateSelectedAccount
                    )
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(uiState.selectedAccount?.accountColor?:MaterialTheme.colorScheme.surfaceVariant.toArgb().toLong()).copy(alpha = 0.05f)
                )
            ){
                Text(
                    text = "Add an Account in the Home Screen!",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_extra_large))
                )
            }
        }
        if(transactions.isNotEmpty()) {
            CategoryPieChart(
                transactionsByType = transactionsByType,
                categories = category,
                selectedCategory = uiState.selectedCategory,
                onSliceClick = viewModel::updateSelectedCategory,
                viewModel,
                uiState = uiState
            )
            MonthlyBarChart(
                selectedYearMonth = uiState.selectedYearMonth,
                listData = transactions.groupByMonth(),
                onClick = viewModel::updateSelectedMonth,
                categories = category,
                navigateToTransactionDetails =navigateToTransactionDetails,
                uiState = uiState
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(uiState.selectedAccount?.accountColor?:MaterialTheme.colorScheme.surfaceVariant.toArgb().toLong()).copy(alpha = 0.05f)
                )
            ){
                Text(
                    text = "Add some Transactions!",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_extra_large))
                )
            }
        }
    }
}

@Composable
fun CategoryPieChart(
    transactionsByType: Map<TransactionType, List<TransactionsWithAccountAndCategory>>,
    categories: List<Category>,
    selectedCategory: Category?,
    onSliceClick: (Category?) -> Unit,
    viewModel: ReportScreenViewModel,
    uiState: ReportsUiState
) {
    val expenseByCategory: Map<Category, List<TransactionsWithAccountAndCategory>> = transactionsByType[TransactionType.EXPENSE]?.groupBy { it.category } ?: emptyMap()
    val incomeByCategory: Map<Category, List<TransactionsWithAccountAndCategory>> = transactionsByType[TransactionType.INCOME]?.groupBy { it.category } ?: emptyMap()
    val expensePieData: List<PieData> = expenseByCategory.toPieDataList()
    val incomePieData: List<PieData> = incomeByCategory.toPieDataList()
    val pagerState = rememberPagerState { 2 }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateSelectedCategory(null)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(uiState.selectedAccount?.accountColor?:MaterialTheme.colorScheme.surfaceVariant.toArgb().toLong()).copy(alpha = 0.05f)
        )
    ) {
        HorizontalPager(pagerState) {
            when(it) {
                0 -> PieChartCard(pieData = expensePieData, onSliceClick = onSliceClick, categories = categories, selectedCategory = selectedCategory, type = "Expenses:")
                1 -> PieChartCard(pieData = incomePieData, onSliceClick = onSliceClick, categories, selectedCategory, type = "Income:")
            }
        }
        Row(
            Modifier
                .height(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(2) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.Gray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(color, CircleShape)
                        .size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun PieChartCard(
    pieData: List<PieData>,
    onSliceClick: (Category?) -> Unit,
    categories: List<Category>,
    selectedCategory: Category?,
    type: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                pieData.forEach { category ->
                    CategoryItem(category)
                }
            }
            PieChart(
                originalExpenses = pieData,
                totalExpense = pieData.sumOf { it.amount },
                onSliceClick = {pieData ->
                    onSliceClick(categories.find {pieData.name == it.categoryName})
                },
                selectedCategory = selectedCategory,
                type = type
            )
        }
        if (selectedCategory != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(selectedCategory.color))
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = selectedCategory.categoryName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val amount: Double = pieData.find { it.name == selectedCategory.categoryName }?.amount ?: 0.0
                    val total = pieData.sumOf { it.amount }
                    val percent: Int = if (total != 0.0) ((amount / total) * 100.0).roundToInt() else 0
                    Text(
                        text = "$${amount}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = "$percent%")
                }
            }
        }
    }

}

@Composable
private fun CategoryItem(category: PieData) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .border(width = 2.dp, color = Color(category.color), shape = CircleShape)
                //.background(Color(0xFFA93636))
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun PieChart(
    originalExpenses: List<PieData>,
    totalExpense: Double,
    onSliceClick: (PieData) -> Unit,
    selectedCategory: Category?,
    type: String
) {

    // Make sure expenses are updated when originalExpenses change
    var expenses by remember { mutableStateOf(originalExpenses) }

    LaunchedEffect(originalExpenses) {
        expenses = originalExpenses
    }

    val sweepAngles = expenses.map { ((it.amount / totalExpense) * 360).toFloat() }
    var startAngle = -90f
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_small))
            .size(200.dp)
    ) {
        val gapAngle = if(expenses.size > 1) {
            12f
        } else {
            0f
        }
        Canvas(
            modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.padding_small))
                .pointerInput(expenses, sweepAngles) {
                    detectTapGestures { offset ->
                        val center = size.center
                        val touchX = offset.x - center.x
                        val touchY = offset.y - center.y
                        val distance = hypot(touchX, touchY)

                        // Calculate the angle of the touch point in degrees
                        val touchAngle = (atan2(touchY, touchX) * 180f / PI.toFloat()).let {
                            if (it < -90f) it + 360f else it
                        }
                        // Only continue if the touch is within the pie circle
                        if (distance <= min(size.width+50, size.height+50) / 2 && distance >= min(size.width - 130, size.height - 130) / 2
                        ) {
                            var start = -90f
                            println(touchAngle)
                            sweepAngles.forEachIndexed { index, sweep ->
                                val sweepWithoutGap =
                                    sweep - gapAngle // Match the gap used in drawing
                                val end = start + sweepWithoutGap

                                if (touchAngle in start..end) {
                                    onSliceClick(expenses[index])
                                    return@detectTapGestures
                                }
                                start += sweep
                            }
                        }
                    }
                }
        ) {

            expenses.forEachIndexed { index, expense ->
                drawArc(
                    color = Color(expense.color),
                    startAngle = startAngle,
                    sweepAngle = sweepAngles[index] - gapAngle,
                    useCenter = false,
                    style = Stroke(
                        width = if (expense.name == selectedCategory?.categoryName) 24.dp.toPx() else 14.dp.toPx(),
                        cap = StrokeCap.Round
                    ),
                    size = Size(size.width, size.height),
                    topLeft = Offset(0f, 0f)
                )
                startAngle += sweepAngles[index]
            }
        }
        Column {
            Text(text = type)
            Text(
                text = "$${totalExpense}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun MonthlyBarChart(
    selectedYearMonth: YearMonth,
    listData: Map<YearMonth, MonthTransactions>,
    onClick: (YearMonth) -> Unit,
    navigateToTransactionDetails: (Int, Int) -> Unit,
    categories: List<Category>,
    uiState: ReportsUiState
) {
    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(uiState.selectedAccount?.accountColor?:MaterialTheme.colorScheme.surfaceVariant.toArgb().toLong()).copy(alpha = 0.05f)
        )
    )  {
        Column(
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.padding_medium),
                end = dimensionResource(R.dimen.padding_medium),
                top = dimensionResource(R.dimen.padding_medium))
        ) {
            BarChart(selectedYearMonth = selectedYearMonth, listData, onClick = onClick)
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            Text(
                text = selectedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            Column {
                val sortedTransactions = listData[selectedYearMonth]?.transactions?.groupByDay() ?: emptyMap()
                val dateList = ArrayList(sortedTransactions.keys)
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

@Composable
fun BarItem(barData: MonthTransactions, isSelected: Boolean, maxValue: Double, onClick: (YearMonth) -> Unit) {
    val maxBarHeight = 190.dp

    // Convert max value and data values to consistent heights
    val incomeHeight = (barData.income / maxValue).toFloat().coerceIn(0f, 1f) * maxBarHeight.value
    val expenseHeight = (barData.expense / maxValue).toFloat().coerceIn(0f, 1f) * maxBarHeight.value
    Column(
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)).clickable { onClick(barData.yearMonth) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(color = if (isSelected) Color.Gray.copy(alpha = 0.5f) else Color.Transparent, shape = RoundedCornerShape(4.dp))
                .padding(4.dp)
        ) {
            Row (verticalAlignment = Alignment.Bottom) {
                Box(
                    modifier = Modifier.width(20.dp)
                        .height(incomeHeight.dp)//((barData.income / maxValue * 200.0).toInt())
                        .background(colorResource(R.color.income), shape = RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier.width(20.dp)
                        .height(expenseHeight.dp)//(barData.expense * 200.0 / maxValue)
                        .background(colorResource(R.color.expense), shape = RoundedCornerShape(4.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.height(18.dp)
        ) {
            Text(text = barData.yearMonth.format(DateTimeFormatter.ofPattern("MMM yy")), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun BarChart(selectedYearMonth: YearMonth, listData: Map<YearMonth, MonthTransactions>, onClick: (YearMonth) -> Unit) {
    val scrollState = rememberScrollState()
    //val maxValue = (listData.maxOfOrNull { maxOf(it.value.income, it.value.expense) } ?: 1.0 ).toInt()
    val rawMax = listData.maxOfOrNull { maxOf(it.value.income, it.value.expense) } ?: 1.0
    val maxValue = (rawMax * 1.1).roundToInt().coerceAtLeast(1)
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(listData.size) {
        coroutineScope.launch {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(modifier = Modifier.padding(bottom = 22.dp).height(200.dp), verticalArrangement = Arrangement.SpaceBetween) {
            //(0..(maxValue) step (maxValue) / 4).reversed().forEach {
            val step = (maxValue / 4.0).roundToInt().coerceAtLeast(1)
            val labels = (step * 4 downTo 0 step step).toMutableList()
            if (!labels.contains(0)) labels.add(0) // Make sure 0 is included
            labels.sortedDescending().forEach {
                Text(
                    text = "$it",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    //modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
        Row(modifier = Modifier.horizontalScroll(scrollState).fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            listData.forEach { (index, barData) ->
                BarItem(
                    barData = barData,
                    isSelected = index == selectedYearMonth,
                    maxValue = maxValue.toDouble(),
                    onClick = onClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsPreview() {
    Card{
        Text(
            text = "Add an Account in the Home Screen!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_extra_large))
        )
    }
}


