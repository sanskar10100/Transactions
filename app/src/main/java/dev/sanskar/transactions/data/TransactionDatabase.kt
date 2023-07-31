package dev.sanskar.transactions.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Transaction::class], version = 3)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE new_Transaction (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, amount INTEGER NOT NULL, timestamp INTEGER NOT NULL, isExpense INTEGER NOT NULL, description TEXT NOT NULL, medium INTEGER NOT NULL)")
        database.execSQL("INSERT INTO new_Transaction (id, amount, timestamp, isExpense, description, medium) SELECT id, amount, timestamp, isExpense, description, CASE WHEN isDigital THEN 1 ELSE 0 END FROM `Transaction`")
        database.execSQL("DROP TABLE `Transaction`")
        database.execSQL("ALTER TABLE new_Transaction RENAME TO `Transaction`")
    }
}