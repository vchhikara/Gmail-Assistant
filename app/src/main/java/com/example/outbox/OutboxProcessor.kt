package com.example.outbox

import com.example.data.AppDatabase
import com.example.gmail.BatchModifyRequest
import com.example.gmail.GmailApi
import retrofit2.HttpException

object PendingAction {
    const val ARCHIVE = "ARCHIVE"
    const val TRASH = "TRASH"
    const val MARK_READ = "MARK_READ"
    const val MARK_UNREAD = "MARK_UNREAD"
}

/**
 * Sends the user's queued actions to Gmail. A row keeps its pendingAction until Gmail confirms,
 * so a failed run is simply retried. A 404 means the message is already gone and counts as done.
 */
class OutboxProcessor(private val api: GmailApi, db: AppDatabase) {
    private val dao = db.emailDao()

    /** Returns how many rows were flushed. Throws on the first failure, leaving the rest queued. */
    suspend fun flush(): Int {
        val pending = dao.getPending()
        var done = 0
        for ((action, rows) in pending.groupBy { it.pendingAction }) {
            val ids = rows.map { it.id }
            for (chunk in ids.chunked(MAX_BATCH)) {
                send(action!!, chunk)
                if (action == PendingAction.TRASH) dao.deleteEmailsByIds(chunk) else dao.clearPending(chunk)
                done += chunk.size
            }
        }
        return done
    }

    private suspend fun send(action: String, ids: List<String>) {
        try {
            when (action) {
                PendingAction.ARCHIVE -> api.batchModify(BatchModifyRequest(ids, removeLabelIds = listOf("INBOX")))
                PendingAction.MARK_READ -> api.batchModify(BatchModifyRequest(ids, removeLabelIds = listOf("UNREAD")))
                PendingAction.MARK_UNREAD -> api.batchModify(BatchModifyRequest(ids, addLabelIds = listOf("UNREAD")))
                PendingAction.TRASH -> ids.forEach { id ->
                    try { api.trash(id) } catch (e: HttpException) { if (e.code() != 404) throw e }
                }
            }
        } catch (e: HttpException) {
            // batchModify answers 404 for the whole call if any id is gone; retry singly so the others still go through.
            if (e.code() != 404 || ids.size == 1) { if (e.code() == 404) return else throw e }
            ids.forEach { send(action, listOf(it)) }
        }
    }

    private companion object { const val MAX_BATCH = 1000 }
}
