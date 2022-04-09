package dev.sanskar.transactions.notifications

import android.content.Context
import androidx.work.*
import dev.sanskar.transactions.TAG_REMINDER_WORKER
import dev.sanskar.transactions.log
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.min

class ReminderNotificationWorker(private val appContext: Context, workerParameters: WorkerParameters) : Worker(appContext, workerParameters) {
    override fun doWork(): Result {
        NotificationHandler.createReminderNotification(appContext)
        return Result.success()
    }

    companion object {

        /**
         * @param hourOfDay the hour at which daily reminder notification should appear [0-23]
         * @param minute the minute at which daily reminder notification should appear [0-59]
         */
        fun schedule(appContext: Context, hourOfDay: Int, minute: Int) {
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
            WorkManager.getInstance(appContext)
                .enqueueUniquePeriodicWork(
                "reminder_notification_work",
                ExistingPeriodicWorkPolicy.REPLACE,
                notificationRequest
            )
        }
    }
}