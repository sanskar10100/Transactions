package dev.sanskar.transactions.ui.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import dev.sanskar.transactions.data.Transaction
import dev.sanskar.transactions.data.TransactionDatabase
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var db = Room.databaseBuilder(
        application,
        TransactionDatabase::class.java,
        "transactions"
    ).fallbackToDestructiveMigration()
        .allowMainThreadQueries()
        .build()

    val transactions = db.transactionDao().getAllTransactions()

    fun addTransaction(
        amount: Int,
        description: String,
        isDigital: Boolean = true,
        isExpense: Boolean = true,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val transaction = Transaction(
            0,
            amount,
            timestamp,
            isExpense,
            description,
            isDigital
        )

        viewModelScope.launch {
            db.transactionDao().insertTransaction(transaction)
        }
        Log.d(TAG, "addTransaction: added $transaction")
    }

    fun updateTransaction(
        id: Int,
        amount: Int,
        description: String,
        isDigital: Boolean = true,
        isExpense: Boolean = true,
        timestamp: Long
    ) {
        val transaction = Transaction(
            id,
            amount,
            timestamp,
            isExpense,
            description,
            isDigital
        )

        viewModelScope.launch {
            db.transactionDao().updateTransaction(transaction)
        }
        Log.d(TAG, "updateTransaction: updated $transaction")
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            db.transactionDao().deleteTransaction(transaction)
        }
        Log.d(TAG, "deleteTransaction: deleted $transaction")
    }

    fun clearTransactions() {
        viewModelScope.launch {
            db.transactionDao().clearTransactions()
        }

        Log.d(TAG, "clearTransactions: all transactions cleared!")
    }
}