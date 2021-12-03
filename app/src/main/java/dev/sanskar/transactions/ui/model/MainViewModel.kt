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

    fun addTransaction(amount: Int, description: String, isDigital: Boolean = true, isExpense: Boolean = true,) {
        val transaction = Transaction(
            0,
            amount,
            System.currentTimeMillis(),
            isExpense,
            description,
            isDigital
        )
        db.transactionDao().insertTransactions(transaction)

        Log.d(TAG, "addTransaction: added $transaction")
    }
}