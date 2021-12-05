package dev.sanskar.transactions.ui.add

import android.util.Log
import androidx.lifecycle.ViewModel
import java.util.*

private const val TAG = "AddTransactionFragmentM"

class AddTransactionFragmentModel : ViewModel() {
    var timestamp = System.currentTimeMillis()
    set(value) {
        // Called every time timestamp is set after the initialization
        updateComponents(value)
    }

    init {
        // For initialization
        updateComponents(timestamp)
    }


    private var year = 0
    private var month = 0
    private var day = 0
    private var hour = 0
    private var minute = 0
    private fun updateComponents(timestamp: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_MONTH)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)

        Log.d(TAG, "updateComponents: Received timestamp: $timestamp, $cal")
    }
}