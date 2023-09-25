package dev.sanskar.transactions.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sanskar.transactions.TransactionMedium
import dev.sanskar.transactions.data.Transaction
import dev.sanskar.transactions.data.TransactionDatabase
import dev.sanskar.transactions.get12HourTime
import dev.sanskar.transactions.log
import dev.sanskar.transactions.toTransactionMedium
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val db: TransactionDatabase
) : ViewModel() {

    var amount = 0
    var description = ""
    var transactionType = TransactionMedium.DIGITAL
    var isExpense = true
    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0

    fun setDateFromTimestamp(timestamp: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        year = calendar[Calendar.YEAR]
        month = calendar[Calendar.MONTH]
        day = calendar[Calendar.DAY_OF_MONTH]
    }

    fun getDate() = "$day/${month + 1}/$year"

    fun getTime() = get12HourTime(hour, minute)

    fun constructTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return calendar.timeInMillis
    }

    private fun setTimeComponents(timestamp: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
    }

    init {
        Calendar.getInstance().apply {
            year = this.get(Calendar.YEAR)
            month = this.get(Calendar.MONTH)
            day = this.get(Calendar.DAY_OF_MONTH)
            hour = this.get(Calendar.HOUR_OF_DAY)
            minute = this.get(Calendar.MINUTE)
        }
    }


    fun setValuesIfEdit(id: Int) = flow {
        val result = db.transactionDao().getTransactionFromId(id)
        if (result != null) {
            amount = result.amount
            description = result.description
            transactionType = result.medium.toTransactionMedium()
            isExpense = result.isExpense
            setTimeComponents(result.timestamp)
            emit(true)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    /**
     * Adds a transaction to the database
     */
    fun addTransaction() {
        val transaction = Transaction(
            0,
            amount,
            constructTimestamp(),
            isExpense,
            description,
            transactionType.ordinal
        )

        viewModelScope.launch {
            db.transactionDao().insertTransaction(transaction)
        }
        log("addTransaction: added $transaction")
    }

    /**
     * Updates a transaction in the database. Transactions are matched through their IDs
     */
    fun updateTransaction(id: Int, ) {
        val transaction = Transaction(
            id,
            amount,
            constructTimestamp(),
            isExpense,
            description,
            transactionType.ordinal
        )

        viewModelScope.launch {
            db.transactionDao().updateTransaction(transaction)
        }
        log("updateTransaction: updated $transaction")
    }
}