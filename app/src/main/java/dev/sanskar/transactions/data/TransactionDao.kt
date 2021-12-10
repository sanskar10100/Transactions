package dev.sanskar.transactions.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transactions: Transaction)

    @Query("SELECT * FROM `transaction` ORDER BY timestamp")
    fun getAllTransactions(): LiveData<List<Transaction>>

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

}