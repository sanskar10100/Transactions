package dev.sanskar.transactions.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transactions: Transaction)

    @Query("SELECT * FROM `transaction`")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Update
    suspend fun updateTransaction(transactions: Transaction)

    @Delete
    suspend fun deleteTransaction(transactions: Transaction)
}