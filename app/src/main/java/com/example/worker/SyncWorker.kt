package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.SmartGmailApp
import com.example.sync.SyncState

/** Background refresh: flushes queued actions, then syncs. Does nothing until the user has signed in. */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val app = SmartGmailApp.instance
        if (!app.isSignedIn()) return Result.success()
        return when (val state = SmartGmailApp.backend.syncAll()) {
            is SyncState.Done -> Result.success()
            SyncState.SignInRequired -> Result.success() // retrying cannot help; the app asks on next open
            is SyncState.Error -> {
                Log.w("SyncWorker", "Sync failed: ${state.message}")
                Result.retry()
            }
            else -> Result.success()
        }
    }
}
