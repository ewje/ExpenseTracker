package com.example.cashexpense.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashexpense.data.AppRepository
import com.example.cashexpense.data.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class CategoriesScreenViewModel(
    private val repository: AppRepository
): ViewModel() {

    var categoryUiState by mutableStateOf(CategoryUiState())
        private set

    private val _categoriesState: StateFlow<List<Category>> = repository.getAllCategoriesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState



    fun updateUiState(categoryDetails: CategoryDetails) {
        categoryUiState =
            CategoryUiState(categoryDetails = categoryDetails)
    }

    private fun validateInput(uiState: CategoryDetails = categoryUiState.categoryDetails): Boolean {
        return with(uiState) {
            name.isNotBlank()
        }
    }

    fun saveCategory() {
        if(validateInput()) {
            viewModelScope.launch {
                repository.insertCategory(categoryUiState.categoryDetails.toCategory())
            }
            categoryUiState = categoryUiState.copy(categoryDetails = CategoryDetails())
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category = category)
        }
    }

    fun isButtonEnabled(): Boolean {
        return categoryUiState.categoryDetails.name.isNotEmpty()
    }

}

data class CategoryUiState(
    val categoryDetails: CategoryDetails = CategoryDetails(),
    val isEntryValid: Boolean = false
)

data class CategoryDetails(
    val color: Long = 0xFFFFFFFF,
    val name: String = ""
)

fun CategoryDetails.toCategory() = Category(
    categoryName =  name,
    color = color
)