package com.example.cashexpense.ui.settings



import android.annotation.SuppressLint
import android.provider.MediaStore.Audio.Media
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.example.cashexpense.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        SettingsBody(modifier = Modifier.padding(innerPadding), navController)
    }
}

@Composable
private fun SettingsBody(modifier: Modifier, navController: NavController) {
    Card(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium))
    ) {
        Column(
            //modifier = Modifier.padding(vertical = 4.dp)
            //verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            SettingsRow(
                text = "Categories",
                onClick = { navController.navigate("Categories") },
                hasArrow = true
            )
            SettingsRow(
                text = "Erase Data",
                onClick = {},
                isLast = true
            )
        }
    }
}

@Composable
private fun SettingsRow(
    text: String,
    onClick: () -> Unit,
    isLast: Boolean = false,
    hasArrow: Boolean = false
) {
    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
            if (hasArrow) {
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = text,
                    tint = Color.Gray
                )
            }
        }
        if (isLast == false) {
            HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)))
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun SettingsBodyPreview() {
    SettingsBody(modifier = Modifier, navController = rememberNavController())
}