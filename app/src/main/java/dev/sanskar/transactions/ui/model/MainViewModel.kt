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
        timestamp: Long,
        isDigital: Boolean = true,
        isExpense: Boolean = true,
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

    fun getDigitalExpense(): Int {
        var digitalBalance = 0

        transactions.value?.forEach { transaction ->
            if (transaction.isDigital) {
                if (transaction.isExpense) {
                    digitalBalance -= transaction.amount
                } else {
                    digitalBalance += transaction.amount
                }
            }
        }

        return digitalBalance
    }

    fun getCashBalance(): Int {
        var cashBalance = 0

        transactions.value?.forEach { transaction ->
            if (!transaction.isDigital) {
                if (transaction.isExpense) {
                    cashBalance -= transaction.amount
                } else {
                    cashBalance += transaction.amount
                }
            }
        }

        return cashBalance
    }

    fun getTotalExpenses(): Int {
        var totalExpense = 0

        transactions.value?.forEach {
            if (it.isExpense) {
                totalExpense += it.amount
            }
        }

        return totalExpense
    }

    fun getCashExpense(): Int {
        var totalCashExpense = 0

        transactions.value?.forEach {
            if (it.isExpense and !it.isDigital) {
                totalCashExpense += it.amount
            }
        }

        return totalCashExpense
    }

    fun getTotalDigitalExpense(): Int {
        var totalDigitalExpense = 0

        transactions.value?.forEach {
            if (it.isExpense and it.isDigital) {
                totalDigitalExpense += it.amount
            }
        }

        return totalDigitalExpense
    }
}