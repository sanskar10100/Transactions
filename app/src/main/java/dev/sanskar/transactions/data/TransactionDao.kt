package dev.sanskar.transactions.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transactions: Transaction)

    @Query("SELECT * FROM `transaction` ORDER BY timestamp")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM `transaction` WHERE amount >= :amount ORDER BY amount DESC",)
    suspend fun filterOnTransactionAmountGreater(amount: Int): List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE amount <= :amount ORDER BY amount DESC",)
    suspend fun filterOnTransactionAmountLesser(amount: Int): List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE amount <= :amount AND isExpense=1 ORDER BY amount DESC",)
    suspend fun filterOnExpenseAmountLesser(amount: Int): List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE amount >= :amount AND isExpense=1 ORDER BY amount DESC",)
    suspend fun filterOnExpenseAmountGreater(amount: Int): List<Transaction>

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 1 AND isDigital = 0")
    fun getTotalCashExpenses(): Int

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 1 AND isDigital = 1")
    fun getTotalDigitalExpenses(): Int

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 0 AND isDigital = 0")
    fun getTotalCashIncome(): Int

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 0 and isDigital = 1")
    fun getTotalDigitalIncome(): Int

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 0")
    fun getTotalIncome(): Int

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 1")
    fun getTotalExpenses(): Int

    @Update
    suspend fun updateTransaction(transactions: Transaction)

    @Delete
    suspend fun deleteTransaction(transactions: Transaction)

    @Query("DELETE FROM `transaction`")
    suspend fun clearTransactions()

    @Query("SELECT SUM(amount) FROM `transaction` WHERE isExpense = 1 AND timestamp >= :timestamp")
    fun getThisWeekExpense(timestamp: Long): Int

    @Query("SELECT * FROM `transaction` WHERE isDigital = 0")
    fun getCashTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM `transaction` WHERE isDigital = 1")
    fun getDigitalTransactions(): Flow<List<Transaction>>
}