package dev.sanskar.transactions.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sanskar.transactions.TAG_REMINDER_WORKER
import dev.sanskar.transactions.log
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * @param hourOfDay the hour at which daily reminder notification should appear [0-23]
     * @param minute the minute at which daily reminder notification should appear [0-59]
     */
    fun scheduleReminderNotification(hourOfDay: Int, minute: Int) {
        log("Reminder scheduling request received for $hourOfDay:$minute")
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        log("Scheduling reminder notification for ${target.timeInMillis - System.currentTimeMillis()} ms from now")

        val notificationRequest = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(24, TimeUnit.HOURS)
            .addTag(TAG_REMINDER_WORKER)
            .setInitialDelay(target.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "reminder_notification_work",
                ExistingPeriodicWorkPolicy.REPLACE,
                notificationRequest
            )
    }

    fun cancelAll() {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_REMINDER_WORKER)
    }
}