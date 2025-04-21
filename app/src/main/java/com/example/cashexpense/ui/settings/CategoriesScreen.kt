package com.example.cashexpense.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashexpense.NavDestination
import com.example.cashexpense.R
import com.example.cashexpense.data.Category
import com.example.cashexpense.ui.AppViewModelProvider
import com.example.cashexpense.ui.transaction.toCategoryDetails
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController


@Composable
fun CategoriesScreen(
    viewModel: CategoriesScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val categories by viewModel.categoriesState.collectAsState()
    CategoriesBody(
        modifier = Modifier,
        categoryUiState = viewModel.categoryUiState,
        onCategoryDetailsChange = viewModel::updateUiState,
        onSaveClick = {
            viewModel.saveCategory()
        },
        categories = categories,
        isButtonEnabled = viewModel.isButtonEnabled(),
        deleteCategory = {category ->
            viewModel.deleteCategory(category)
        }
    )

}

@Composable
private fun CategoriesBody(
    modifier: Modifier,
    categoryUiState: CategoryUiState,
    onCategoryDetailsChange: (CategoryDetails) -> Unit,
    onSaveClick: () -> Unit,
    categories: List<Category>,
    isButtonEnabled: Boolean,
    deleteCategory: (Category) -> Unit
) {
    Column(
        modifier = modifier.padding(dimensionResource(R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        CategoryAdd(
            modifier = Modifier,
            categoryDetails = categoryUiState.categoryDetails,
            onCategoryDetailsChange = onCategoryDetailsChange,
            onSaveClick = onSaveClick,
            isButtonEnabled = isButtonEnabled
        )
        Card(
            modifier = Modifier
        ) {
            LazyColumn {
                items(categories) {category ->
                    val isLast = category == categories.lastOrNull()

                    CategoryRow(
                        category = category,
                        isLast = isLast,
                        onClick = { categoryDetails ->
                            onCategoryDetailsChange(categoryDetails)
                            println(categoryDetails)
                        },
                        onDelete = { deleteCategory(category) }
                    )
                }
            }
        }
    }

}

@Composable
private fun CategoryAdd(
    modifier: Modifier,
    categoryDetails: CategoryDetails,
    onCategoryDetailsChange: (CategoryDetails) -> Unit,
    onSaveClick: () -> Unit,
    isButtonEnabled: Boolean
) {
    val controller = rememberColorPickerController()
    val openDialog = remember { mutableStateOf(false) }
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(width = 2.dp, color = Color(categoryDetails.color), shape = CircleShape)
                    .clickable { openDialog.value = true }
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .background(Color(categoryDetails.color))
                )
            }
            TextField(
                value = categoryDetails.name,
                onValueChange = {
                    onCategoryDetailsChange(categoryDetails.copy(name = it))
                },
                label = { Text("Add Category") },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
            FilledIconButton(
                onClick = { onSaveClick() },
                enabled = isButtonEnabled
            ) {
                Icon(Icons.Outlined.Done, contentDescription = "Save")
            }
        }

        if (openDialog.value) {
            ColorPicker(
                controller = controller,
                onDismissRequest = {
                    openDialog.value = false
                },
                saveColor = {
                    onCategoryDetailsChange(categoryDetails.copy(color = controller.selectedColor.value.toArgb().toLong()))
                },
                initialColor = Color(categoryDetails.color)
            )
        }
    }
}

@Composable
fun ColorPicker(
    controller: ColorPickerController,
    onDismissRequest: () -> Unit,
    saveColor: () -> Unit,
    initialColor: Color
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                Text(
                    text = "Select Category Color",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(
                    thickness = 2.dp,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))
                )
                AlphaTile(
                    modifier = Modifier
                        .height(35.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp)),
                    controller = controller
                )
                Box(
                    modifier = Modifier
                        .size(214.dp)
                        .clip(CircleShape)
                        .border(width = 4.dp, color = Color.White, shape = CircleShape)
                ) {
                    HsvColorPicker(
                        modifier = Modifier
                            .height(200.dp)
                            .align(Alignment.Center),
                        controller = controller,
                        onColorChanged = {},
                        initialColor = initialColor
                    )
                }
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    controller = controller
                )
                TextButton(
                    onClick = {
                        onDismissRequest()
                        saveColor()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

object CategoriesDestination: NavDestination {
    override val route = "categories"
    override val title = "Categories"
}

@Composable
private fun CategoryRow(
    category: Category,
    onClick: (CategoryDetails) -> Unit,
    onDelete: () -> Unit,
    isLast: Boolean = false
) {
    Column(
        modifier = Modifier.clickable { onClick(category.toCategoryDetails()) }
    ) {
        println(category)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                    //.border(width = 2.dp, color = Color(category.color), shape = CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .align(Alignment.Center)
                            .background(Color(category.color))
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "",
                modifier = Modifier.clickable { onDelete() }
                //tint = Color.Gray
            )

        }
        if (!isLast) {
            HorizontalDivider(
                thickness = 2.dp,
                modifier = Modifier
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryScreenPreview() {
    CategoryRow(
        category = Category(categoryName = "Hello", color = 0xFFFFFFFF),
        onClick = {},
        onDelete = {}
    )
}