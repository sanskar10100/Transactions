package dev.sanskar.transactions.data

import androidx.room.Database

@Database(entities = [Transaction::class], version = 1)
abstract class TransactionDatabase {
    abstract fun transactionDao(): TransactionDao
}