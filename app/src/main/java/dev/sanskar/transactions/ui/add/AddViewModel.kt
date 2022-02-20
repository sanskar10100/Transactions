package dev.sanskar.transactions.ui.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sanskar.transactions.data.DBInstanceHolder
import dev.sanskar.transactions.data.Transaction
import dev.sanskar.transactions.log
import kotlinx.coroutines.launch
import java.util.*

class AddViewModel : ViewModel() {
    // Datetime related code
    var timestamp: Long
        set(value) {
            // Called every time timestamp is set after the initialization
            // I'm not sure why this exists, but at this point I'm too afraid to ask
            updateComponents(value)
        }
        get() {
            Calendar.getInstance().apply {
                set(year, month, day, hour, minute)
                return this.timeInMillis
            }
        }

    init {
        // For initialization
        timestamp = System.currentTimeMillis()
        updateComponents(timestamp)
    }


    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0

    private fun updateComponents(timestamp: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_MONTH)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)

        log("updateComponents: Received timestamp: $timestamp, $cal")
    }

    // Persistence related code

    private val db = DBInstanceHolder.db

    val updateTransaction = MutableLiveData<Transaction>()
    fun getTransactionForUpdate(id: Int) {
        viewModelScope.launch {
            val result = db.transactionDao().getTransactionFromId(id)
            if (result != null) {
                updateTransaction.value = result!!
            }
        }
    }

    /**
     * Adds a transaction to the database
     */
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
        log("addTransaction: added $transaction")
    }

    /**
     * Updates a transaction in the database. Transactions are matched through their IDs
     */
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
        log("updateTransaction: updated $transaction")
    }
}