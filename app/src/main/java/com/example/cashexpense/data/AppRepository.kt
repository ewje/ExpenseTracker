package com.example.cashexpense.data

import kotlinx.coroutines.flow.Flow

interface AppRepository {

    val account: Flow<List<Account>>

    fun getAllCategoriesStream(): Flow<List<Category>>

    fun getCategoryStream(id: Int): Flow<Category?>

    fun getAllTransactionsStream(): Flow<List<Transaction>>

    fun getTransactionStream(id: Int): Flow<Transaction>

    fun getAllAccountsStream(): Flow<List<Account>>

    fun getAccountStream(id: Int): Flow<Account>

    fun getTransactionsWithAccountAndCategory(): Flow<List<TransactionsWithAccountAndCategory>>

    fun getTransactionsWithAccountAndCategoryByAccount(id: Int): Flow<List<TransactionsWithAccountAndCategory>>

    fun getTransactionWithAccountAndCategory(id: Int): Flow<TransactionsWithAccountAndCategory>

    suspend fun deleteTransactionsByAccountId(id: Int)

    suspend fun deleteTransactionsByCategoryId(id: Int)

    suspend fun insertCategory(category: Category)

    suspend fun deleteCategory(category: Category)

    suspend fun insertTransaction(transaction: Transaction)

    suspend fun deleteTransaction(transaction: Transaction)

    suspend fun insertAccount(account: Account)

    suspend fun updateAccount(account: Account)

    suspend fun deleteAccount(account: Account)



}