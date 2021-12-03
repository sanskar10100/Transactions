package dev.sanskar.transactions.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransactions(vararg transactions: Transaction)

    @Query("SELECT * FROM `transaction`")
    fun getAllTransactions(): List<Transaction>
}