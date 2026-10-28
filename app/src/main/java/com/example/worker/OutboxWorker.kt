package com.example.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.SmartGmailApp
import com.example.sync.SyncState
import java.util.concurrent.TimeUnit

/** Writes the user's archive, delete and read changes to Gmail; retried with backoff until it succeeds. */
class OutboxWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = SmartGmailApp.instance
        if (!app.isSignedIn()) return Result.success()
        return when (SmartGmailApp.backend.flushOutbox()) {
            is SyncState.Error -> Result.retry()
            else -> Result.success()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<OutboxWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork("OutboxFlush", ExistingWorkPolicy.REPLACE, request)
        }
    }
}
