package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {
    @Query("SELECT * FROM emails WHERE isArchived = 0 ORDER BY timestamp DESC")
    fun getAllEmails(): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails WHERE category = :category AND isArchived = 0 ORDER BY timestamp DESC")
    fun getEmailsByCategory(category: String): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails WHERE id = :id LIMIT 1")
    suspend fun getEmailById(id: String): EmailEntity?

    /** Legacy bulk write. Replaces rows and so wipes local state; prefer [EmailStore.merge]. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailEntity>)

    /** Inserts only rows that do not exist yet; returns the rowId per item, -1 when skipped. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(emails: List<EmailEntity>): List<Long>

    /** Refreshes server-owned fields only. Read, archive, summary and manual category are untouched. */
    @Query(
        "UPDATE emails SET threadId = :threadId, sender = :sender, senderEmail = :senderEmail, " +
            "subject = :subject, snippet = :snippet, timestamp = :timestamp, " +
            "labelIds = :labelIds, isUnread = :isUnread WHERE id = :id"
    )
    suspend fun updateServerFields(
        id: String, threadId: String, sender: String, senderEmail: String,
        subject: String, snippet: String, timestamp: Long, labelIds: String, isUnread: Boolean
    )

    @Query(
        "UPDATE emails SET category = :category, isClutter = :isClutter, " +
            "isUnsubscribeSuggested = :unsub WHERE id = :id AND categoryManual = 0"
    )
    suspend fun updateClassification(id: String, category: String, isClutter: Boolean, unsub: Boolean)

    @Query("UPDATE emails SET pendingAction = :action WHERE id IN (:ids)")
    suspend fun setPendingAction(ids: List<String>, action: String?)

    @Query("SELECT id FROM emails WHERE isArchived = 0 AND pendingAction IS NULL")
    suspend fun getActiveIdsWithoutPending(): List<String>

    @Query("SELECT * FROM emails WHERE pendingAction IS NOT NULL AND id NOT LIKE 'demo\\_%' ESCAPE '\\'")
    suspend fun getPending(): List<EmailEntity>

    @Query("DELETE FROM emails WHERE id = :id")
    suspend fun deleteEmailById(id: String)

    @Query("DELETE FROM emails WHERE id IN (:ids)")
    suspend fun deleteEmailsByIds(ids: List<String>)

    @Query("UPDATE emails SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: String, isRead: Boolean)

    @Query("UPDATE emails SET isRead = :isRead WHERE id IN (:ids)")
    suspend fun updateReadStatusForBulk(ids: List<String>, isRead: Boolean)

    /** User read/unread: updates the local view and queues the Gmail write. */
    @Query("UPDATE emails SET isRead = :isRead, pendingAction = :pending WHERE id IN (:ids)")
    suspend fun setReadAndQueue(ids: List<String>, isRead: Boolean, pending: String)

    @Query("UPDATE emails SET isArchived = 1, pendingAction = 'ARCHIVE' WHERE id = :id")
    suspend fun archiveEmail(id: String)

    @Query("UPDATE emails SET isArchived = 1, pendingAction = 'ARCHIVE' WHERE id IN (:ids)")
    suspend fun archiveEmailsForBulk(ids: List<String>)

    /** Hides rows now and queues a Gmail trash; the outbox removes them once the server confirms. */
    @Query("UPDATE emails SET isArchived = 1, pendingAction = 'TRASH' WHERE id IN (:ids)")
    suspend fun trashEmails(ids: List<String>)

    /** Archive applied because the server already did it; nothing to write back. */
    @Query("UPDATE emails SET isArchived = 1 WHERE id IN (:ids) AND pendingAction IS NULL")
    suspend fun markArchivedFromServer(ids: List<String>)

    @Query("UPDATE emails SET pendingAction = NULL WHERE id IN (:ids)")
    suspend fun clearPending(ids: List<String>)

    @Query("UPDATE emails SET category = :category, categoryManual = 1 WHERE id = :id")
    suspend fun updateCategory(id: String, category: String)

    @Query("UPDATE emails SET summary = :summary, suggestedActions = :actions WHERE id = :id")
    suspend fun updateSummaryAndActions(id: String, summary: String?, actions: String?)

    @Query("DELETE FROM emails WHERE id LIKE 'demo\\_%' ESCAPE '\\'")
    suspend fun deleteDemo()

    @Query("DELETE FROM emails")
    suspend fun clearAll()
}

@Dao
interface SyncStateDao {
    @Query("SELECT value FROM sync_state WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(state: SyncStateEntity)

    @Query("DELETE FROM sync_state WHERE `key` = :key")
    suspend fun remove(key: String)
}
