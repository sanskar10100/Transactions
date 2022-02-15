package dev.sanskar.transactions.ui.add

import androidx.lifecycle.ViewModel
import dev.sanskar.transactions.log
import java.util.*

class AddTransactionFragmentModel : ViewModel() {
    var timestamp: Long
    set(value) {
        // Called every time timestamp is set after the initialization
        // I'm not sure why this exists, but at this point I'm too afraid to ask
        updateComponents(value)
    }
    get() {
        Calendar.getInstance().apply {
            set(year, month, day, hour, minute)
            return this.timeInMillis
        }
    }

    init {
        // For initialization
        timestamp = System.currentTimeMillis()
        updateComponents(timestamp)
    }


    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0

    private fun updateComponents(timestamp: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_MONTH)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)

        log("updateComponents: Received timestamp: $timestamp, $cal")
    }
}