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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailEntity>)

    @Query("DELETE FROM emails WHERE id = :id")
    suspend fun deleteEmailById(id: String)

    @Query("DELETE FROM emails WHERE id IN (:ids)")
    suspend fun deleteEmailsByIds(ids: List<String>)

    @Query("UPDATE emails SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: String, isRead: Boolean)

    @Query("UPDATE emails SET isRead = :isRead WHERE id IN (:ids)")
    suspend fun updateReadStatusForBulk(ids: List<String>, isRead: Boolean)

    @Query("UPDATE emails SET isArchived = 1 WHERE id = :id")
    suspend fun archiveEmail(id: String)

    @Query("UPDATE emails SET isArchived = 1 WHERE id IN (:ids)")
    suspend fun archiveEmailsForBulk(ids: List<String>)

    @Query("UPDATE emails SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: String, category: String)

    @Query("UPDATE emails SET summary = :summary, suggestedActions = :actions WHERE id = :id")
    suspend fun updateSummaryAndActions(id: String, summary: String?, actions: String?)

    @Query("DELETE FROM emails")
    suspend fun clearAll()
}
