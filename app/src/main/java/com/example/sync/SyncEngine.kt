package com.example.sync

import com.example.classify.Classifier
import com.example.data.AppDatabase
import com.example.data.EmailStore
import com.example.data.ServerEmail
import com.example.data.SyncStateEntity
import com.example.gmail.GmailApi
import com.example.gmail.toMeta
import com.example.gmail.toServerEmail
import retrofit2.HttpException

data class SyncResult(val added: Int, val updated: Int, val removed: Int, val fullResync: Boolean)

sealed interface SyncState {
    data object Idle : SyncState
    data object Running : SyncState
    data class Done(val result: SyncResult) : SyncState
    /** Auth is missing or expired; the UI should ask the user to sign in again. */
    data object SignInRequired : SyncState
    data class Error(val message: String) : SyncState
}

/**
 * Pulls the Gmail inbox into Room. First run does a full fetch and stores the mailbox historyId;
 * later runs replay `history.list` from it. Local user state is never overwritten.
 */
class SyncEngine(
    private val api: GmailApi,
    private val db: AppDatabase,
    private val classifier: Classifier,
    private val maxInbox: Int = 100
) {
    private val store = EmailStore(db)
    private val dao = db.emailDao()
    private val state = db.syncStateDao()

    suspend fun sync(): SyncResult {
        val start = state.get(HISTORY_KEY) ?: return full()
        return try {
            incremental(start)
        } catch (e: HttpException) {
            if (e.code() == 404) full() else throw e
        }
    }

    private suspend fun full(): SyncResult {
        // Read the historyId first so changes made while we fetch are replayed next time.
        val historyId = api.getProfile().historyId
        val ids = LinkedHashSet<String>()
        var page: String? = null
        do {
            val res = api.listMessages(maxResults = minOf(100, maxInbox - ids.size), pageToken = page)
            res.messages.orEmpty().forEach { ids += it.id }
            page = res.nextPageToken
        } while (page != null && ids.size < maxInbox)

        val fetched = fetch(ids)
        val inserted = store.merge(fetched).size
        classify(fetched)

        // Anything we still show as active but the server no longer has in INBOX was archived elsewhere.
        val stale = dao.getActiveIdsWithoutPending().filter { it !in ids }
        if (stale.isNotEmpty()) dao.markArchivedFromServer(stale)

        state.put(SyncStateEntity(HISTORY_KEY, historyId))
        return SyncResult(inserted, fetched.size - inserted, stale.size, fullResync = true)
    }

    private suspend fun incremental(startId: String): SyncResult {
        val touched = LinkedHashSet<String>()
        val deleted = LinkedHashSet<String>()
        var latest = startId
        var page: String? = null
        do {
            val res = api.listHistory(startHistoryId = startId, pageToken = page)
            res.history.orEmpty().forEach { rec ->
                rec.messagesAdded?.forEach { m -> m.message?.id?.let(touched::add) }
                rec.labelsAdded?.forEach { m -> m.message?.id?.let(touched::add) }
                rec.labelsRemoved?.forEach { m -> m.message?.id?.let(touched::add) }
                rec.messagesDeleted?.forEach { m -> m.message?.id?.let(deleted::add) }
            }
            res.historyId?.let { latest = it }
            page = res.nextPageToken
        } while (page != null)

        touched -= deleted
        if (deleted.isNotEmpty()) dao.deleteEmailsByIds(deleted.toList())

        val fetched = fetch(touched)
        val inserted = store.merge(fetched).size
        classify(fetched)
        val leftInbox = fetched.filter { "INBOX" !in it.labelIds }.map { it.id }
        if (leftInbox.isNotEmpty()) dao.markArchivedFromServer(leftInbox)

        state.put(SyncStateEntity(HISTORY_KEY, latest))
        return SyncResult(inserted, fetched.size - inserted, deleted.size, fullResync = false)
    }

    /** Messages deleted between listing and fetching come back as 404 and are skipped. */
    private suspend fun fetch(ids: Collection<String>): List<ServerEmail> =
        ids.mapNotNull { id ->
            try {
                api.getMessage(id).toServerEmail()
            } catch (e: HttpException) {
                if (e.code() == 404) null else throw e
            }
        }

    private suspend fun classify(items: List<ServerEmail>) {
        for (e in items) {
            val c = classifier.classify(e.toMeta())
            dao.updateClassification(e.id, c.category, c.isClutter, c.isUnsubscribeSuggested)
        }
    }

    companion object {
        const val HISTORY_KEY = "historyId"
    }
}
