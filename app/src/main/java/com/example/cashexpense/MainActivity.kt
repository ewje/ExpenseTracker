package com.example.cashexpense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cashexpense.ui.home.HomeDestination
import com.example.cashexpense.ui.home.HomeScreen
import com.example.cashexpense.ui.home.TransactionDetailsDestination
import com.example.cashexpense.ui.home.TransactionDetailsScreen
import com.example.cashexpense.ui.reports.ReportDestination
import com.example.cashexpense.ui.reports.ReportsScreen
import com.example.cashexpense.ui.reports.TransactionsDestination
import com.example.cashexpense.ui.reports.TransactionsScreen
import com.example.cashexpense.ui.settings.CategoriesDestination
import com.example.cashexpense.ui.settings.CategoriesScreen
import com.example.cashexpense.ui.settings.SettingsDestination
import com.example.cashexpense.ui.settings.SettingsScreen
import com.example.cashexpense.ui.theme.CashExpenseTheme
import com.example.cashexpense.ui.transaction.TransactionEditDestination
import com.example.cashexpense.ui.transaction.TransactionEditScreen
import com.example.cashexpense.ui.transaction.TransactionEntryDestination
import com.example.cashexpense.ui.transaction.TransactionEntryScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashExpenseTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBarContent(currentRoute, navController)
                    },
                    bottomBar = { if(currentRoute in bottomNavDestination.map { it.route}) {
                        NavigationBar(currentRoute, navController)
                    }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "Home",
                        modifier = Modifier.padding(innerPadding)) {
                        composable(route = HomeDestination.route) {
                            HomeScreen(navigateToTransactionDetails = { id1, id2 ->
                                val base = TransactionDetailsDestination.routeWithoutArgs
                                val route = if (id2 != -1) {
                                    "$base/$id1?${
                                        TransactionDetailsDestination.transactionId2
                                    }=$id2"
                                } else {
                                    "$base/$id1"
                                }
                                navController.navigate(route)
                            })
                        }
                        composable(route = ReportDestination.route) {
                            ReportsScreen(navigateToTransactionDetails = { id1, id2 ->
                                val base = TransactionDetailsDestination.routeWithoutArgs
                                val route = if (id2 != -1) {
                                    "$base/$id1?${
                                        TransactionDetailsDestination.transactionId2
                                    }=$id2"
                                } else {
                                    "$base/$id1"
                                }
                                navController.navigate(route)
                            })
                        }
                        composable(route = TransactionEntryDestination.route) {
                            TransactionEntryScreen(
                                navigateBack = { navController.navigate(HomeDestination.route) }
                            )
                        }
                        composable(
                            route = TransactionDetailsDestination.route,
                            arguments = listOf(
                                navArgument(TransactionDetailsDestination.transactionId1) {
                                    type = NavType.IntType
                                },
                                navArgument(TransactionDetailsDestination.transactionId2) {
                                    type = NavType.IntType
                                    defaultValue = -1
                                }
                            )
                        ){
                            TransactionDetailsScreen(
                                navigateBack = { navController.navigate(HomeDestination.route) },
                                navigateToTransactionEdit = { id1, id2 ->
                                    val base = TransactionEditDestination.routeWithoutArgs
                                    val route = if (id2 != -1) {
                                        "$base/$id1?${
                                            TransactionEditDestination.transactionId2
                                        }=$id2"
                                    } else {
                                        "$base/$id1"
                                    }
                                    navController.navigate(route)
                                }
                            )
                        }
                        composable(
                            route = TransactionEditDestination.route,
                            arguments = listOf(
                                navArgument(TransactionEditDestination.transactionId1) {
                                    type = NavType.IntType
                                },
                                navArgument(TransactionEditDestination.transactionId2) {
                                    type = NavType.IntType
                                    defaultValue = -1
                                }
                            )
                        ){
                            TransactionEditScreen(
                                navigateBack = { navController.navigate(HomeDestination.route) }
                            )
                        }
                        composable(route = SettingsDestination.route) {
                            SettingsScreen(navController)
                        }
                        composable(route = CategoriesDestination.route) {
                            CategoriesScreen()
                        }
                        composable(route = TransactionsDestination.route) {
                            TransactionsScreen(navigateToTransactionDetails = { id1, id2 ->
                                val base = TransactionDetailsDestination.routeWithoutArgs
                                val route = if (id2 != -1) {
                                    "$base/$id1?${
                                        TransactionDetailsDestination.transactionId2
                                    }=$id2"
                                } else {
                                    "$base/$id1"
                                }
                                navController.navigate(route)
                            })
                        }
                    }
                }
            }
        }
    }
}

interface NavDestination {
    val route: String
    val title: String
}

val bottomNavDestination = listOf(
    HomeDestination,
    TransactionEntryDestination,
    SettingsDestination,
    ReportDestination,
    TransactionsDestination
)
val allDestinations = listOf(
    HomeDestination,
    TransactionEntryDestination,
    TransactionDetailsDestination,
    TransactionEditDestination,
    SettingsDestination,
    CategoriesDestination,
    ReportDestination,
    TransactionsDestination
)

@Composable
fun NavigationBar(
    currentRoute: String?,
    navController: NavHostController
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar {
            NavigationBarItem(
                selected = currentRoute == HomeDestination.route,
                onClick = {
                    if (currentRoute != HomeDestination.route) {
                        navController.navigate(HomeDestination.route)
                    }
                },
                label = {
                    Text(text = HomeDestination.title)
                },
                icon = {
                    Icon(
                        imageVector = if(currentRoute == HomeDestination.route) {
                            HomeDestination.selectedIcon
                        } else HomeDestination.unselectedIcon,
                        contentDescription = HomeDestination.title
                    )
                }
            )
            NavigationBarItem(
                selected = currentRoute == ReportDestination.route,
                onClick = {
                    if (currentRoute != ReportDestination.route) {
                        navController.navigate(ReportDestination.route)
                    }
                },
                label = {
                    Text(text = ReportDestination.title)
                },
                icon = {
                    Icon(
                        imageVector = if(currentRoute == ReportDestination.route) {
                            ReportDestination.selectedIcon
                        } else ReportDestination.unselectedIcon,
                        //painterResource(id = R.drawable.insert_chart_24px),
                        contentDescription = ReportDestination.title
                    )
                }
            )
            Spacer(
                modifier = Modifier.weight(1f)
            )
            NavigationBarItem(
                selected = currentRoute == TransactionsDestination.route,
                onClick = {
                    if (currentRoute != TransactionsDestination.route) {
                        navController.navigate(TransactionsDestination.route)
                    }
                },
                label = {
                    Text(text = TransactionsDestination.NAVTITLE)
                },
                icon = {
                    Icon(
                        painterResource(R.drawable.payments_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                        contentDescription = TransactionEntryDestination.title
                    )
                }
            )
            NavigationBarItem(
                selected = currentRoute == SettingsDestination.route,
                onClick = {
                    if (currentRoute != SettingsDestination.route) {
                        navController.navigate(SettingsDestination.route)
                    }
                },
                label = {
                    Text(text = SettingsDestination.title)
                },
                icon = {
                    Icon(
                        imageVector = if(currentRoute == SettingsDestination.route) {
                            SettingsDestination.selectedIcon
                        } else SettingsDestination.unselectedIcon,
                        contentDescription = SettingsDestination.title
                    )
                }
            )
        }
        FloatingActionButton(
            onClick = {
                if (currentRoute != TransactionEntryDestination.route) {
                    navController.navigate(TransactionEntryDestination.route)
                }
            },
            shape = CircleShape,
            modifier = Modifier.offset(y = (-8).dp).align(Alignment.BottomCenter).padding(bottom = 56.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "add transaction"
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContent(
    currentRoute: String?,
    navController: NavHostController
) {
    val destination = allDestinations.find {it.route == currentRoute}
    val showBackButton = currentRoute !in bottomNavDestination.map {it.route}
    destination?.let {
        TopAppBar(
            title = { Text(
                text = it.title,
                style = MaterialTheme.typography.headlineLarge
            ) },
            navigationIcon = { if(showBackButton) {
                IconButton(onClick = {navController.popBackStack()}) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } }
        )
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CashExpenseTheme {
        Greeting("hello")
    }
}

