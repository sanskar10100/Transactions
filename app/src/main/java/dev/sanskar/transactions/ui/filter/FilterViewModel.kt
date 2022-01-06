package dev.sanskar.transactions.ui.filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sanskar.transactions.data.DBInstanceHolder
import dev.sanskar.transactions.data.Transaction
import kotlinx.coroutines.launch

class FilterViewModel() : ViewModel() {

    // Get Database Instance from singleton object
    private var db = DBInstanceHolder.db
    private var amount = 0
    private var onlyExpense = true
    private var greaterThan = true

    val filteredTransactions = MutableLiveData<List<Transaction>>()

    fun filterOnAmount(amount: Int, greaterThan: Boolean, onlyExpenses: Boolean) {
        this.amount = amount
        this.greaterThan = greaterThan
        this.onlyExpense = onlyExpenses
        viewModelScope.launch {
            if (onlyExpenses && greaterThan) {
                filteredTransactions.value = db.transactionDao().filterOnExpenseAmountGreater(amount)
            } else if (onlyExpenses && !greaterThan) {
                filteredTransactions.value = db.transactionDao().filterOnExpenseAmountLesser(amount)
            } else if (!onlyExpenses && greaterThan) {
                filteredTransactions.value = db.transactionDao().filterOnTransactionAmountGreater(amount)
            } else if (!onlyExpenses && !greaterThan) {
                filteredTransactions.value = db.transactionDao().filterOnTransactionAmountLesser(amount)
            }
        }
    }

    fun getParameterDescription(): String {
        var result = ""
        result += if (onlyExpense) "All expenses with amount " else "All transactions with amount "
        result += if (greaterThan) "> " else "< "
        result += "$amount"
        return result
    }

    fun clearParameterDescription() {
        amount = 0
        onlyExpense = true
        greaterThan = true
    }
}