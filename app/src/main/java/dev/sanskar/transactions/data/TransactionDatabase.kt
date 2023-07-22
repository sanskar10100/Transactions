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
        // Create the new transactionType column
        database.execSQL("ALTER TABLE `Transaction` ADD COLUMN `transactionType` INTEGER NOT NULL DEFAULT 0")

        // Set the transactionType value based on the isDigital value
        database.execSQL("UPDATE `Transaction` SET `transactionType` = 0 WHERE `isDigital` = 0")
        database.execSQL("UPDATE `Transaction` SET `transactionType` = 1 WHERE `isDigital` = 1")

        // Remove the old isDigital column
        database.execSQL("CREATE TABLE `Transaction_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `description` TEXT NOT NULL, `isExpense` INTEGER NOT NULL, `transactionType` INTEGER NOT NULL)")
        database.execSQL("INSERT INTO `Transaction_new` (`id`, `amount`, `timestamp`, `description`, `isExpense`, `transactionType`) SELECT `id`, `amount`, `timestamp`, `description`, `isDigital`, `transactionType` FROM `Transaction`")
        database.execSQL("DROP TABLE `Transaction`")
        database.execSQL("ALTER TABLE `Transaction_new` RENAME TO `Transaction`")
    }
}