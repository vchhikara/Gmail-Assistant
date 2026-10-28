package com.example

import android.content.Context
import com.example.auth.AuthRepository
import com.example.auth.AuthRequiredException
import com.example.classify.RuleClassifier
import com.example.data.AppDatabase
import com.example.gmail.GmailApi
import com.example.gmail.GmailClient
import com.example.outbox.OutboxProcessor
import com.example.summary.NanoSummarizer
import com.example.summary.RuleSummarizer
import com.example.summary.Summarizer
import com.example.sync.SyncEngine
import com.example.sync.SyncState
import java.io.IOException
import retrofit2.HttpException

/** Wires the on-device pipeline. One instance per process, owned by [SmartGmailApp]. */
class Backend(context: Context, val db: AppDatabase) {
    val auth = AuthRepository(context)
    val classifier = RuleClassifier()
    val summarizer: Summarizer = NanoSummarizer(context, fallback = RuleSummarizer())
    private val api: GmailApi by lazy { GmailClient.create(auth) }

    suspend fun flushOutbox(): SyncState = guarded { OutboxProcessor(api, db).flush(); SyncState.Idle }

    /** Sends queued user actions, then pulls new mail. Failures come back as state, never as fake data. */
    suspend fun syncAll(): SyncState = guarded {
        OutboxProcessor(api, db).flush()
        SyncState.Done(SyncEngine(api, db, classifier).sync())
    }

    private suspend fun guarded(block: suspend () -> SyncState): SyncState = try {
        block()
    } catch (e: AuthRequiredException) {
        SyncState.SignInRequired
    } catch (e: HttpException) {
        if (e.code() == 401 || e.code() == 403) SyncState.SignInRequired
        else SyncState.Error("Gmail returned ${e.code()}")
    } catch (e: IOException) {
        SyncState.Error("No connection to Gmail")
    }
}
