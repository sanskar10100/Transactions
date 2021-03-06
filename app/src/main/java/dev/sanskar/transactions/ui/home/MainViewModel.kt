package dev.sanskar.transactions.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sanskar.transactions.DEFAULT_REMINDER_HOUR
import dev.sanskar.transactions.DEFAULT_REMINDER_MINUTE
import dev.sanskar.transactions.data.*
import dev.sanskar.transactions.log
import dev.sanskar.transactions.notifications.NotificationScheduler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefStore: PreferenceStore,
    private val db: TransactionDatabase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {


    fun scheduleReminderNotification(hourOfDay: Int, minute: Int) {
        prefStore.setReminderTime(hourOfDay, minute)
        notificationScheduler.scheduleReminderNotification(hourOfDay, minute)
    }

    fun getReminderTime() = prefStore.getReminderTime()

    fun cancelReminderNotification() {
        log("Cancelling reminder notification")
        prefStore.cancelReminder()
        notificationScheduler.cancelAll()
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
        var searchChoice = SearchChoices.UNSPECIFIED
        var searchQuery = ""
        var filterTimeChoice = FilterByTimeChoices.UNSPECIFIED
        var filterFromTime = -1L
        var filterToTime = -1L
    }

    // The main transactions list livedata. This list is used as the central reference throughout the app
    val transactions = MutableLiveData<List<Transaction>>()

    init {
        resetQueryConfig()
        executeConfig() // Get first time data with initial configurations
        checkAndSetDefaultReminder()
    }

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

    fun setSearchQuery(query: String) {
        QueryConfig.searchChoice = SearchChoices.SPECIFIED
        QueryConfig.searchQuery = query
        executeConfig()
    }

    fun setFilterTime(fromTime: Long, toTime: Long) {
        QueryConfig.filterTimeChoice = FilterByTimeChoices.SPECIFIED
        QueryConfig.filterFromTime = fromTime
        QueryConfig.filterToTime = toTime
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
            .setFilterSearch(QueryConfig.searchChoice, QueryConfig.searchQuery)
            .setFilterTime(QueryConfig.filterTimeChoice, QueryConfig.filterFromTime, QueryConfig.filterToTime)
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
            searchChoice = SearchChoices.UNSPECIFIED
            searchQuery = ""
            filterTimeChoice = FilterByTimeChoices.UNSPECIFIED
            filterFromTime = -1
            filterToTime = -1
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

    fun shouldAskForReview() = liveData {
        val result = db.transactionDao().getTransactionCount()
        if (result in listOf(10, 25, 50, 100, 200, 500, 1000)) emit(true)
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