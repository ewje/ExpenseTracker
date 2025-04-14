package com.example.cashexpense.ui.reports

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.runtime.Composable
import com.example.cashexpense.Greeting
import com.example.cashexpense.NavDestination

object ReportDestination: NavDestination {
    override val route = "reports"
    override val title = "Reports"
    val selectedIcon = Icons.Filled.ShoppingCart
    val unselectedIcon = Icons.Outlined.ShoppingCart
}

@Composable
fun ReportsScreen() {
    Greeting("Reports")
}