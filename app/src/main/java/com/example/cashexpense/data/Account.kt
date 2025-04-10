package com.example.cashexpense.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    val accountName: String,
    val accAmount: Double,
    val income: Double,
    val expense: Double,
    val accountColor: Long,
    @ColumnInfo(name = "account_id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)