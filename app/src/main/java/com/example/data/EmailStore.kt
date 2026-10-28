package com.example.data

import androidx.room.withTransaction

/** Server-owned view of one message, as produced by a sync. */
data class ServerEmail(
    val id: String,
    val threadId: String,
    val sender: String,
    val senderEmail: String,
    val subject: String,
    val snippet: String,
    val timestamp: Long,
    val labelIds: List<String>,
    /** Lower-case header map used for classification; not stored. */
    val headers: Map<String, String> = emptyMap()
) {
    val isUnread: Boolean get() = "UNREAD" in labelIds
}

/** Writes sync results without destroying what the user did locally. */
class EmailStore(private val db: AppDatabase) {
    private val dao = db.emailDao()

    /**
     * Inserts new messages and refreshes server fields of known ones.
     * Read, archived, summary, category and pending actions on existing rows are kept.
     * Returns the ids that were new.
     */
    suspend fun merge(items: List<ServerEmail>): List<String> = db.withTransaction {
        val newRows = items.map { it.toNewEntity() }
        val rowIds = dao.insertIfAbsent(newRows)
        val inserted = ArrayList<String>()
        items.forEachIndexed { i, e ->
            if (rowIds[i] == -1L) {
                dao.updateServerFields(
                    e.id, e.threadId, e.sender, e.senderEmail, e.subject, e.snippet,
                    e.timestamp, e.labelIds.joinToString(","), e.isUnread
                )
            } else {
                inserted += e.id
            }
        }
        inserted
    }

    private fun ServerEmail.toNewEntity() = EmailEntity(
        id = id, threadId = threadId, sender = sender, senderEmail = senderEmail,
        subject = subject, snippet = snippet, timestamp = timestamp,
        isRead = !isUnread, isArchived = "INBOX" !in labelIds,
        category = "Uncategorized",
        labelIds = labelIds.joinToString(","), isUnread = isUnread
    )
}
