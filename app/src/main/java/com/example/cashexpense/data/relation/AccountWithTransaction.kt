package com.example.cashexpense.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashexpense.data.Account
import com.example.cashexpense.data.Transaction

data class AccountWithTransaction(
    @Embedded val account: Account,
    @Relation(
        parentColumn = "accountName",
        entityColumn = "accountName"
    )
    val transaction: List<Transaction>
)
