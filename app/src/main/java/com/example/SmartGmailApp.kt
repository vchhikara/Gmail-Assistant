package com.example

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.AppDatabase
import com.example.worker.SyncWorker
import java.util.concurrent.TimeUnit

class SmartGmailApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Log.i("SmartGmailApp", "Smart Gmail Manager starting up...")
        
        // Initialize Room DB
        database = AppDatabase.getDatabase(this)
        
        // Schedule background work manager sync every 30 minutes
        scheduleSyncWorker()
    }

    private fun scheduleSyncWorker() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SyncGmailMetadata",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            Log.i("SmartGmailApp", "Scheduled SyncedWorker with WorkManager every 30 mins.")
        } catch (e: Exception) {
            Log.e("SmartGmailApp", "Failed to schedule SyncWorker with WorkManager (expected in testing or fallback environments): ${e.message}")
        }
    }

    companion object {
        lateinit var instance: SmartGmailApp
            private set
            
        lateinit var database: AppDatabase
            private set
    }
}
