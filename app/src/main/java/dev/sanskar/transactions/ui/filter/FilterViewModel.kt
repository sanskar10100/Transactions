package dev.sanskar.transactions.ui.filter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import dev.sanskar.transactions.data.Transaction
import dev.sanskar.transactions.data.TransactionDatabase
import kotlinx.coroutines.launch

class FilterViewModel(application: Application) : AndroidViewModel(application) {
    private var db = Room.databaseBuilder(
        application,
        TransactionDatabase::class.java,
        "transactions"
    ).fallbackToDestructiveMigration()
        .allowMainThreadQueries()
        .build()

    val filteredTransactions = MutableLiveData<List<Transaction>>()

    fun filterOnAmount(amount: Int) {
        viewModelScope.launch {
            filteredTransactions.value = db.transactionDao().filterOnAmountGreater(amount)
        }
    }
}