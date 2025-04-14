package com.example.cashexpense.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cashexpense.ui.transaction.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Comparator


@Entity(tableName = "transactions")
data class Transaction(
    val title: String,
    val transAmount: Double,
    val type: TransactionType,
    val date: Long,
    val details: String,
    @ColumnInfo(name = "transaction_id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val accountIdFk: Int,
    val categoryIdFk: Int
)

data class TransactionsWithAccountAndCategory(
    @Embedded val transaction: Transaction,
    @Embedded val account: Account,
    @Embedded val category: Category
)

data class DayTransactions(
    val transactions: MutableList<TransactionsWithAccountAndCategory>,
    val total: Double
)

fun List<TransactionsWithAccountAndCategory>.groupByDay(): Map<LocalDate, DayTransactions> {
    val dataMap: MutableMap<LocalDate, DayTransactions> = mutableMapOf()


    this.forEach { transaction ->
        val date =
            Instant.ofEpochMilli(transaction.transaction.date)           // Convert count-of-milliseconds-since-epoch into a date-time in UTC (`Instant`).
                .atZone(ZoneId.systemDefault())           // Adjust into the wall-clock time used by the people of a particular region (a time zone). Produces a `ZonedDateTime` object.
                .toLocalDate()

        if (dataMap[date] == null) {
            dataMap[date] = DayTransactions(
                transactions = mutableListOf(),
                total = 0.0
            )
        }

        dataMap[date]?.transactions?.add(transaction)

        if (transaction.transaction.type == TransactionType.INCOME) {
            dataMap[date]?.total?.plus(transaction.transaction.transAmount)
        } else {
            dataMap[date]?.total?.minus(transaction.transaction.transAmount)
        }
    }

    return dataMap.toSortedMap(Comparator.reverseOrder())
}

fun LocalDate.formatDate(): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val myFormatObj = DateTimeFormatter.ofPattern(" MMMM dd")


    return when {
        this.isEqual(today) -> "Today, ${this.format(myFormatObj)}"
        this.isEqual(yesterday) -> "Yesterday, ${this.format(myFormatObj)}"
        this.year != today.year -> this.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd uuuu"))
        else -> this.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
    }
}

