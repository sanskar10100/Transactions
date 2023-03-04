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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefStore: PreferenceStore,
    private val db: TransactionDatabase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    val filterState = MutableStateFlow(FilterState())
    val backClearsFilter = filterState
        .map { it.areFiltersActive() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

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

    // The main transactions list livedata. This list is used as the central reference throughout the app
    val transactions = MutableLiveData<List<Transaction>>()

    init {
        resetFilters()
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

    fun getDigitalBalance() =
        db.transactionDao().getTotalDigitalIncome() - db.transactionDao().getTotalDigitalExpenses()

    fun getCashBalance() =
        db.transactionDao().getTotalCashIncome() - db.transactionDao().getTotalCashExpenses()

    fun getTotalExpenses() = db.transactionDao().getTotalExpenses().toFloat()

    fun getCashExpense() = db.transactionDao().getTotalCashExpenses().toFloat()

    fun getDigitalExpense() = db.transactionDao().getTotalDigitalExpenses()

    /**
     * Sets the sorting method (highest amount/lowest amount/earliest/latest/insertion order (default))
     */
    fun setSortMethod(index: Int) {
        filterState.value = filterState.value.copy(sortChoice = SortByChoices.values().find {
            it.ordinal == index
        } ?: SortByChoices.UNSPECIFIED_TIME_EARLIEST_FIRST)
        executeConfig()
    }

    /**
     * Sets the filter type (income/expense/both)
     */
    fun setFilterType(index: Int) {
        filterState.value = filterState.value.copy(
            filterTypeChoice = FilterByTypeChoices.values().find {
                it.ordinal == index
            } ?: FilterByTypeChoices.UNSPECIFIED)
        executeConfig()
    }

    /**
     * Sets the filter medium (cash/digital/both)
     */
    fun setFilterMedium(index: Int) {
        filterState.value =
            filterState.value.copy(filterMediumChoice = FilterByMediumChoices.values().find {
                it.ordinal == index
            } ?: FilterByMediumChoices.UNSPECIFIED)
        executeConfig()
    }

    /**
     * Sets the filter amount type (less than/greater than) and the amount.
     */
    fun setFilterAmount(amount: Int, index: Int) {
        filterState.value = filterState.value.copy(
            filterAmountChoice = FilterByAmountChoices.values().find {
                it.ordinal == index
            } ?: FilterByAmountChoices.UNSPECIFIED,
            filterAmountValue = amount
        )
        executeConfig()
    }

    fun setSearchQuery(query: String) {
        filterState.value = filterState.value.copy(
            searchChoice = SearchChoices.SPECIFIED,
            searchQuery = query
        )
        executeConfig()
    }

    fun setFilterTime(fromTime: Long, toTime: Long) {
        filterState.value = filterState.value.copy(
            filterTimeChoice = FilterByTimeChoices.SPECIFIED,
            filterFromTime = fromTime,
            filterToTime = toTime
        )
        executeConfig()
    }

    /**
     * Generates an SQL query from the current configuration and executes it through Room.
     * An observable flow is returned, updates whenever there's a change in the database
     */
    private fun executeConfig() {
        val query = QueryBuilder()
            .setFilterAmount(
                filterState.value.filterAmountChoice,
                filterState.value.filterAmountValue
            )
            .setFilterType(filterState.value.filterTypeChoice)
            .setFilterMedium(filterState.value.filterMediumChoice)
            .setSortingChoice(filterState.value.sortChoice)
            .setFilterSearch(filterState.value.searchChoice, filterState.value.searchQuery)
            .setFilterTime(
                filterState.value.filterTimeChoice,
                filterState.value.filterFromTime,
                filterState.value.filterToTime
            )
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
    fun resetFilters() {
        filterState.value = FilterState()
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
}