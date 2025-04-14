package com.example.cashexpense.data

import androidx.compose.animation.core.updateTransition
import kotlinx.coroutines.flow.Flow

class OfflineRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) : AppRepository {
    override val account = accountDao.getAllAccounts()
    //val TransactionsWithAccountAndCategory = transactionDao.getTransactionsWithAccountAndCategory()

    override fun getAllCategoriesStream(): Flow<List<Category>> = categoryDao.getAllCategories()

    override fun getCategoryStream(id: Int): Flow<Category?> = categoryDao.getCategory(id)

    override fun getAllTransactionsStream(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    override fun getTransactionStream(id: Int): Flow<Transaction> = transactionDao.getTransaction(id)

    override fun getAllAccountsStream(): Flow<List<Account>> = accountDao.getAllAccounts()

    override fun getAccountStream(id: Int): Flow<Account> = accountDao.getAccount(id)

    override fun getTransactionsWithAccountAndCategory(): Flow<List<TransactionsWithAccountAndCategory>> = transactionDao.getTransactionsWithAccountAndCategory()

    override fun getTransactionsWithAccountAndCategoryByAccount(id: Int): Flow<List<TransactionsWithAccountAndCategory>> =
        transactionDao.getTransactionsWithAccountAndCategoryByAccount(id)

    override fun getTransactionWithAccountAndCategory(id: Int): Flow<TransactionsWithAccountAndCategory> =
        transactionDao.getTransactionWithAccountAndCategory(id)

    override suspend fun insertCategory(category: Category){
        categoryDao.insertCategory(category)
    }

    override suspend fun updateCategory(category: Category){
        categoryDao.updateCategory(category)
    }

    override suspend fun deleteCategory(category: Category){
        categoryDao.deleteCategory(category)
    }

    override suspend fun insertTransaction(transaction: Transaction){
        transactionDao.insertTransaction(transaction)
    }

    override suspend fun updateTransaction(transaction: Transaction){
        transactionDao.updateTransaction(transaction)
    }

    override suspend fun deleteTransaction(transaction: Transaction){
        transactionDao.deleteTransaction(transaction)
    }

    override suspend fun insertAccount(account: Account){
        accountDao.insertAccount(account)
    }

    override suspend fun updateAccount(account: Account){
        accountDao.updateAccount(account)
    }

    override suspend fun deleteAccount(account: Account){
        accountDao.deleteAccount(account)
    }


}