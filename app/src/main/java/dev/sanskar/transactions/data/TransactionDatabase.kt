package dev.sanskar.transactions.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class], version = 2)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}

object DBInstanceHolder {
    lateinit var db: TransactionDatabase
}