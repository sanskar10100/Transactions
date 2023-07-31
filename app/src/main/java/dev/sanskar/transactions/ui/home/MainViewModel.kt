package dev.sanskar.transactions.ui.home

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sanskar.transactions.DEFAULT_REMINDER_HOUR
import dev.sanskar.transactions.DEFAULT_REMINDER_MINUTE
import dev.sanskar.transactions.TransactionMedium
import dev.sanskar.transactions.asFormattedDateTime
import dev.sanskar.transactions.data.*
import dev.sanskar.transactions.formattedName
import dev.sanskar.transactions.log
import dev.sanskar.transactions.notifications.NotificationScheduler
import dev.sanskar.transactions.oneShotFlow
import dev.sanskar.transactions.toTransactionMedium
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefStore: PreferenceStore,
    private val db: TransactionDao,
    private val notificationScheduler: NotificationScheduler,
    private val contentResolver: ContentResolver
) : ViewModel() {

    val filterState = MutableStateFlow(FilterState())
    val backClearsFilter = filterState
        .map { it.areFiltersActive() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val message = oneShotFlow<String>()

    // The main transactions list livedata. This list is used as the central reference throughout the app
    val transactions = MutableStateFlow(emptyList<Transaction>())
    val cashBalance = MutableStateFlow(0)
    val digitalBalance = MutableStateFlow(0)
    val creditBalance = MutableStateFlow(0)

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
            db.clearTransactions()
        }

        log("clearTransactions: all transactions cleared!")
    }

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
    fun setFilterMedium(medium: FilterByMediumChoices) {
        filterState.value = filterState.value.copy(filterMediumChoice = medium)
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
        viewModelScope.launch {
            db
                .customTransactionQuery(buildQuery(filterState.value))
                .collect {
                    transactions.value = it

                    // I haven't quite figured out how to do this in SQL because of the complexity of the query
                    digitalBalance.value = it
                        .filter { it.transactionType == TransactionMedium.DIGITAL.ordinal }
                        .fold(0) { acc, transaction ->
                            if (transaction.isExpense) (acc - transaction.amount) else (acc + transaction.amount)
                        }

                    cashBalance.value = it
                        .filter { it.transactionType == TransactionMedium.CASH.ordinal }
                        .fold(0) { acc, transaction ->
                            if (transaction.isExpense) (acc - transaction.amount) else (acc + transaction.amount)
                        }

                    creditBalance.value = it.
                    filter { it.transactionType == TransactionMedium.CREDIT.ordinal }
                        .fold(0) { acc, transaction ->
                            if (transaction.isExpense) (acc - transaction.amount) else (acc + transaction.amount)
                        }
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
            db.deleteTransaction(transaction)
        }
        log("deleteTransaction: deleted $transaction")
    }

    /**
     * Finds and deletes a transaction
     */
    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            val transactionToDelete = db.getTransactionFromId(id)
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
            val transactionToDelete = transactions.value[position]
            deleteTransaction(transactionToDelete)
        }
    }

    fun shouldAskForReview() = flow {
        val result = db.getTransactionCount()
        emit(result in listOf(10, 25, 50, 100, 200, 500, 1000))
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    /**
     * Restores any deleted transactions
     */
    fun undoTransactionDelete() {
        if (clearedTransaction != null) {
            viewModelScope.launch {
                db.insertTransaction(clearedTransaction!!)
                clearedTransaction = null
            }
        }
    }

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
     * Exports a CSV of all available transactions to the
     * downloads folder on the user's device
     */
    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            val transactions = db.getAllTransactions()
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                val writer = outputStream?.bufferedWriter()
                writer?.append("ID,Amount,Type,Medium,Time,Description\n")
                transactions.forEach {
                    writer?.append("${it.id}," +
                            "${it.amount}," +
                            "${if (it.isExpense) "Expense" else "Income"}," +
                            "${it.transactionType.toTransactionMedium().formattedName}," +
                            "${it.timestamp.asFormattedDateTime()}," +
                            "${it.description}\n"
                    )
                }
                writer?.flush()
                writer?.close()
                outputStream?.close()
                message.tryEmit("CSV exported")
            } catch (e: IOException) {
                log("exportCsv: ${e.message}")
                message.tryEmit("Error exporting CSV")
            }
        }
    }
}