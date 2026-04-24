package com.pocketfm.analytics.internal

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

internal class Dispatcher(
    context: Context,
    private val flushIntervalMinutes: Long,
) {
    private val appContext = context.applicationContext

    fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<DispatchWorker>(flushIntervalMinutes, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun triggerNow() {
        val request = OneTimeWorkRequestBuilder<DispatchWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(appContext).enqueue(request)
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "pfm_analytics_periodic_dispatch"
    }
}
