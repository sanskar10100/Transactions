package dev.sanskar.transactions.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sanskar.transactions.data.TransactionDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val db: TransactionDao
) : ViewModel() {

    fun getExpensesSinceMidnight() = flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        emit(db.getTotalExpenseSinceTimestamp(calendar.timeInMillis))
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun getTotalExpenses() = db.getExpenses().toFloat()

    fun getCashExpense() = db.getCashExpenses().toFloat()

    fun getDigitalExpense() = db.getDigitalExpenses()

}