package com.example.cashexpense.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashexpense.data.Category
import com.example.cashexpense.data.Transaction

data class CategoryWithTransaction(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "categoryName",
        entityColumn = "categoryName"
    )
    val transaction: List<Transaction>
)
