package dev.sanskar.transactions.ui.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import dev.sanskar.transactions.data.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class MainViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Holds the current Query Configurations like the applied filters and the sorting method.
     * executeConfig() must always be executed after a change is made to any of the below params.
     * NOTE: The values do not persist between usage sessions.
     */
    object QueryConfig {
        var filterAmountChoice = FilterByAmountChoices.UNSPECIFIED
        var filterAmountValue = -1
        var filterTypeChoice = FilterByTypeChoices.UNSPECIFIED
        var filterMediumChoice = FilterByMediumChoices.UNSPECIFIED
        var sortChoice = SortByChoices.UNSPECIFIED
    }

    private var db = Room.databaseBuilder(
        application,
        TransactionDatabase::class.java,
        "transactions"
    ).allowMainThreadQueries() // For small aggregate queries like sum
        .build()


    init {
        DBInstanceHolder.db = this.db
        resetQueryConfig()
        executeConfig() // Get first time data with initial configurations
    }

    // The main transactions list livedata. This list is used as the central reference throughout the app
    val transactions = MutableLiveData<List<Transaction>>()

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
        Log.d(TAG, "addTransaction: added $transaction")
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
        Log.d(TAG, "updateTransaction: updated $transaction")
    }

    /**
     * Purges the transaction record from the database. Use very cautiously.
     */
    fun clearAllTransactions() {
        viewModelScope.launch {
            db.transactionDao().clearTransactions()
        }

        Log.d(TAG, "clearTransactions: all transactions cleared!")
    }

    fun getDigitalBalance() = db.transactionDao().getTotalDigitalIncome() - db.transactionDao().getTotalDigitalExpenses()

    fun getCashBalance() = db.transactionDao().getTotalCashIncome() - db.transactionDao().getTotalCashExpenses()

    fun getTotalExpenses() = db.transactionDao().getTotalExpenses().toFloat()

    fun getCashExpense() = db.transactionDao().getTotalCashExpenses().toFloat()

    fun getDigitalExpense() = db.transactionDao().getTotalDigitalExpenses()

    /**
     * Sets the sorting method (highest amount/lowest amount/earliest/latest/insertion order (default))
     */
    fun setSortMethod(index: Int) {
        QueryConfig.sortChoice = SortByChoices.values().find {
            it.ordinal == index
        } ?: SortByChoices.UNSPECIFIED
        executeConfig()
    }

    /**
     * Sets the filter type (income/expense/both)
     */
    fun setFilterType(index: Int) {
        QueryConfig.filterTypeChoice = FilterByTypeChoices.values().find {
            it.ordinal == index
        } ?: FilterByTypeChoices.UNSPECIFIED
        executeConfig()
    }

    /**
     * Sets the filter medium (cash/digital/both)
     */
    fun setFilterMedium(index: Int) {
        QueryConfig.filterMediumChoice = FilterByMediumChoices.values().find {
            it.ordinal == index
        } ?: FilterByMediumChoices.UNSPECIFIED
        executeConfig()
    }

    /**
     * Sets the filter amount type (less than/greater than) and the amount.
     */
    fun setFilterAmount(amount: Int, index: Int) {
        QueryConfig.filterAmountChoice = FilterByAmountChoices.values().find {
            it.ordinal == index
        } ?: FilterByAmountChoices.UNSPECIFIED
        QueryConfig.filterAmountValue = amount
        executeConfig()
    }

    /**
     * Generates an SQL query from the current configuration and executes it through Room.
     * An observable flow is returned, updates whenever there's a change in the database
     */
    private fun executeConfig() {
        val query = QueryBuilder()
            .setFilterAmount(QueryConfig.filterAmountChoice, QueryConfig.filterAmountValue)
            .setFilterType(QueryConfig.filterTypeChoice)
            .setFilterMedium(QueryConfig.filterMediumChoice)
            .setSortingChoice(QueryConfig.sortChoice)
            .build()
        viewModelScope.launch {
            db.transactionDao().customTransactionQuery(query).collect {
                transactions.value = it
            }
        }
    }

    /**
     * Resets query configuration to its original state
     */
    fun resetQueryConfig() {
        QueryConfig.apply {
            filterAmountChoice = FilterByAmountChoices.UNSPECIFIED
            filterAmountValue = -1
            filterTypeChoice = FilterByTypeChoices.UNSPECIFIED
            filterMediumChoice = FilterByMediumChoices.UNSPECIFIED
            sortChoice = SortByChoices.UNSPECIFIED
        }
        executeConfig()
    }

    private var clearedTransaction: Transaction? = null

    /**
    * Deletes a transaction from the database. Matched through ID
    */
    fun deleteTransaction(transaction: Transaction) {
        clearedTransaction = transaction
        viewModelScope.launch {
            db.transactionDao().deleteTransaction(transaction)
        }
        Log.d(TAG, "deleteTransaction: deleted $transaction")
    }

    /**
     * Finds and deletes a transaction
     */
    fun deleteTransaction(index: Int) {
        val transactionToDelete = transactions.value?.get(index)
        if (transactionToDelete != null) {
            deleteTransaction(transactionToDelete)
        }
    }

    /**
     * Restores any deleted transactions
     */
    fun undoTransactionDelete() {
        if (clearedTransaction != null) {
            viewModelScope.launch {
                db.transactionDao().insertTransaction(clearedTransaction!!)
                clearedTransaction = null
            }
        }
    }

    val searchResults = MutableLiveData<List<Transaction>>()

    /**
     * Generates search results bases on the query
     */
    fun search(searchQuery: String) {
        viewModelScope.launch {
            searchResults.value = db.transactionDao().search("%$searchQuery%")
        }
    }
}