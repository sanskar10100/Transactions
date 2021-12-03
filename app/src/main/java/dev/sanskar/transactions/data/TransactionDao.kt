package dev.sanskar.transactions.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(vararg transactions: Transaction)

    @Query("SELECT * FROM `transaction`")
    suspend fun getAllTransactions(): List<Transaction>
}