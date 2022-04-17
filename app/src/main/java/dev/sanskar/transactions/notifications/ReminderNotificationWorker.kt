package dev.sanskar.transactions.notifications

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sanskar.transactions.TAG_REMINDER_WORKER
import dev.sanskar.transactions.log
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderNotificationWorker(private val appContext: Context, workerParameters: WorkerParameters) : Worker(appContext, workerParameters) {
    override fun doWork(): Result {
        NotificationHandler.createReminderNotification(appContext)
        return Result.success()
    }
}