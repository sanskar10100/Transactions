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
    var parameterDescription = ""

    val filteredTransactions = MutableLiveData<List<Transaction>>()

    fun filterOnAmount(amount: Int, greaterThan: Boolean) {
        viewModelScope.launch {
            if (greaterThan) {
                parameterDescription = "All Transactions with amount > $amount"
                filteredTransactions.value = db.transactionDao().filterOnAmountGreater(amount)
            } else {
                parameterDescription = "All Transactions with amount < $amount"
                filteredTransactions.value = db.transactionDao().filterOnAmountLesser(amount)
            }
        }
    }
}