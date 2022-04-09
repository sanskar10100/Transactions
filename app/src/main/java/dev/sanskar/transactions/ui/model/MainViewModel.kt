package dev.sanskar.transactions.ui.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.work.WorkManager
import dev.sanskar.transactions.DEFAULT_REMINDER_HOUR
import dev.sanskar.transactions.DEFAULT_REMINDER_MINUTE
import dev.sanskar.transactions.TAG_REMINDER_WORKER
import dev.sanskar.transactions.data.*
import dev.sanskar.transactions.log
import dev.sanskar.transactions.notifications.ReminderNotificationWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "MainViewModel"

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val prefStore = PreferenceStore(application)

    fun scheduleReminderNotification(hourOfDay: Int, minute: Int) {
        prefStore.setReminderTime(hourOfDay, minute)
        ReminderNotificationWorker.schedule(app, hourOfDay, minute)
    }

    fun getReminderTime() = prefStore.getReminderTime()

    fun cancelReminderNotification() {
        log("Cancelling reminder notification")
        prefStore.cancelReminder()
        WorkManager.getInstance(app).cancelAllWorkByTag(TAG_REMINDER_WORKER)
    }

    /**
     * This should run only once, on the first launch of the app and sets a default reminder time
     * which can then be configured by the user.
     */
    private fun checkAndSetDefaultReminder() {
        if (!prefStore.isDefaultReminderSet()) {
            scheduleReminderNotification(DEFAULT_REMINDER_HOUR, DEFAULT_REMINDER_MINUTE)
            prefStore.saveDefaultReminderIsSet()
        }
    }

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
        checkAndSetDefaultReminder()
    }

    // The main transactions list livedata. This list is used as the central reference throughout the app
    val transactions = MutableLiveData<List<Transaction>>()

    /**
     * Purges the transaction record from the database. Use very cautiously.
     */
    fun clearAllTransactions() {
        viewModelScope.launch {
            db.transactionDao().clearTransactions()
        }

        log("clearTransactions: all transactions cleared!")
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
    private fun deleteTransaction(transaction: Transaction) {
        clearedTransaction = transaction
        viewModelScope.launch {
            db.transactionDao().deleteTransaction(transaction)
        }
        log("deleteTransaction: deleted $transaction")
    }

    /**
     * Finds and deletes a transaction
     */
    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            val transactionToDelete = db.transactionDao().getTransactionFromId(id)
            if (transactionToDelete != null) {
                deleteTransaction(transactionToDelete)
            }
        }
    }

    /**
     * This is potentially risky, as list position can change depending on filters
     */
    fun deleteTransactionByPosition(position: Int) {
        viewModelScope.launch {
            val transactionToDelete = transactions.value?.get(position)
            if (transactionToDelete != null) {
                deleteTransaction(transactionToDelete)
            }
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