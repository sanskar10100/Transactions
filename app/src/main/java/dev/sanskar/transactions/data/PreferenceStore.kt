package dev.sanskar.transactions.data

import android.content.Context
import androidx.core.content.edit
import dev.sanskar.transactions.SHARED_PREF_REMINDER_HOUR
import dev.sanskar.transactions.SHARED_PREF_REMINDER_MINUTE

class PreferenceStore(private val context: Context) {
    private val sharedPref = context.getSharedPreferences("transactions_shared_pref", Context.MODE_PRIVATE)

    /**
     * @return the hour of the day in which the reminder should be shown, default is 22, if cancelled -1
     * @return the minute at which the reminder should be shown, default is 0, if cancelled -1
     */
    fun getReminderTime(): Pair<Int, Int> {
        return Pair(
            sharedPref.getInt(SHARED_PREF_REMINDER_HOUR, 22),
            sharedPref.getInt(SHARED_PREF_REMINDER_MINUTE, 0)
        )
    }

    fun setReminderTime(hour: Int, minute: Int) {
        sharedPref.edit {
            putInt(SHARED_PREF_REMINDER_HOUR, hour)
            putInt(SHARED_PREF_REMINDER_MINUTE, minute)
        }
    }

    fun cancelReminder() {
        sharedPref.edit {
            putInt(SHARED_PREF_REMINDER_HOUR, -1)
            putInt(SHARED_PREF_REMINDER_MINUTE, -1)
        }
    }

    fun isDefaultReminderSet() = sharedPref.getBoolean("is_default_reminder_set", false)

    fun saveDefaultReminderIsSet() {
        sharedPref.edit { putBoolean("is_default_reminder_set", true) }
    }
}