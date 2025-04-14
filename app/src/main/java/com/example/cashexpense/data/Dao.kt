package com.example.cashexpense.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cashexpense.data.relation.AccountWithTransaction
import com.example.cashexpense.data.relation.CategoryWithTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE transaction_id = :id")
    fun getTransaction(id: Int): Flow<Transaction>

    @Query("SELECT * FROM transactions ORDER BY date ASC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions AS T INNER JOIN categories AS C
        ON T.categoryIdFk = C.category_id INNER JOIN accounts AS A
        ON T.accountIdFk = A.account_id
    """)
    fun getTransactionsWithAccountAndCategory(): Flow<List<TransactionsWithAccountAndCategory>>

    @Query("""
        SELECT * FROM transactions AS T INNER JOIN categories AS C
        ON T.categoryIdFk = C.category_id INNER JOIN accounts AS A
        ON T.accountIdFk = A.account_id WHERE A.account_id = :id
    """)
    fun getTransactionsWithAccountAndCategoryByAccount(id: Int): Flow<List<TransactionsWithAccountAndCategory>>

    @Query("""
        SELECT * FROM transactions AS T INNER JOIN categories AS C
        ON T.categoryIdFk = C.category_id INNER JOIN accounts AS A
        ON T.accountIdFk = A.account_id WHERE T.transaction_id = :id
    """)
    fun getTransactionWithAccountAndCategory(id: Int): Flow<TransactionsWithAccountAndCategory>
}

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("SELECT * from accounts WHERE account_id = :id")
    fun getAccount(id: Int): Flow<Account>

    @Query("SELECT * from accounts ORDER BY account_id ASC")
    fun getAllAccounts(): Flow<List<Account>>
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * from categories WHERE category_id = :id")
    fun getCategory(id: Int): Flow<Category>

    @Query("SELECT * from categories ORDER BY category_id")
    fun getAllCategories(): Flow<List<Category>>
}