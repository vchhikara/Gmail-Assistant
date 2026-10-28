package com.example.data

import com.example.outbox.PendingAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Local source of truth for the UI. User actions change Room immediately and queue the matching
 * Gmail write (see OutboxProcessor); network sync lives in SyncEngine.
 */
class EmailRepository(private val emailDao: EmailDao) {

    val allEmails: Flow<List<EmailEntity>> = emailDao.getAllEmails()

    fun getEmailsByCategory(category: String): Flow<List<EmailEntity>> = emailDao.getEmailsByCategory(category)

    suspend fun getEmailById(id: String): EmailEntity? = emailDao.getEmailById(id)

    suspend fun updateReadStatus(id: String, isRead: Boolean) = updateReadStatusForBulk(listOf(id), isRead)

    suspend fun updateReadStatusForBulk(ids: List<String>, isRead: Boolean) = withContext(Dispatchers.IO) {
        emailDao.setReadAndQueue(ids, isRead, if (isRead) PendingAction.MARK_READ else PendingAction.MARK_UNREAD)
    }

    suspend fun archiveEmail(id: String) = withContext(Dispatchers.IO) { emailDao.archiveEmail(id) }

    suspend fun archiveEmailsForBulk(ids: List<String>) = withContext(Dispatchers.IO) { emailDao.archiveEmailsForBulk(ids) }

    /** Moves to Gmail trash (recoverable there for 30 days), not a permanent delete. */
    suspend fun deleteEmail(id: String) = deleteEmailsForBulk(listOf(id))

    suspend fun deleteEmailsForBulk(ids: List<String>) = withContext(Dispatchers.IO) { emailDao.trashEmails(ids) }

    suspend fun setCategory(id: String, category: String) = withContext(Dispatchers.IO) {
        emailDao.updateCategory(id, category)
    }

    suspend fun saveSummary(id: String, summary: String, actions: List<String>) = withContext(Dispatchers.IO) {
        emailDao.updateSummaryAndActions(id, summary, actions.joinToString(","))
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) { emailDao.clearAll() }
}
