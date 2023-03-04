package dev.sanskar.transactions.data

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transactions: Transaction)

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 1 AND isDigital = 0")
    fun getCashExpenses(): Int

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 1 AND isDigital = 1")
    fun getDigitalExpenses(): Int

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 1")
    fun getExpenses(): Int

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 0 AND isDigital = 0")
    fun getCashIncome(): Int

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 0 and isDigital = 1")
    fun getDigitalIncome(): Int


    @Update
    suspend fun updateTransaction(transactions: Transaction)

    @Delete
    suspend fun deleteTransaction(transactions: Transaction)

    @Query("DELETE FROM `transaction`")
    suspend fun clearTransactions()

    @RawQuery(observedEntities = [Transaction::class])
    fun customTransactionQuery(query: SupportSQLiteQuery): Flow<List<Transaction>>

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    suspend fun getTransactionFromId(id: Int): Transaction?

    @Query("SELECT COUNT(*) FROM `transaction`")
    suspend fun getTransactionCount(): Int
}