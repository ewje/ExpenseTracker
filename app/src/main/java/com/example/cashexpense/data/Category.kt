package com.example.cashexpense.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    val categoryName: String,
    val color: Long,
    @ColumnInfo(name = "category_id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)