package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emails")
data class EmailEntity(
    @PrimaryKey val id: String, // Gmail API's unique message ID
    val threadId: String,
    val sender: String,
    val senderEmail: String,
    val subject: String,
    val snippet: String,
    val timestamp: Long,
    val isRead: Boolean,
    val isArchived: Boolean,
    val category: String, // "Work", "Bill", "Newsletter", "Promotion", "Social", "Uncategorized"
    val isUnsubscribeSuggested: Boolean = false,
    val isClutter: Boolean = false,
    val summary: String? = null,
    val suggestedActions: String? = null, // comma-separated or custom actions
    /** Gmail label ids as last seen on the server, comma-separated. */
    @ColumnInfo(defaultValue = "") val labelIds: String = "",
    /** Server-side unread state (UNREAD label). Local [isRead] is the user's view and is never overwritten by sync. */
    @ColumnInfo(defaultValue = "0") val isUnread: Boolean = false,
    /** Queued Gmail write: "ARCHIVE", "TRASH", "MARK_READ", or null when nothing is pending. */
    @ColumnInfo(defaultValue = "NULL") val pendingAction: String? = null,
    /** True once the user picked the category by hand; classification must not overwrite it. */
    @ColumnInfo(defaultValue = "0") val categoryManual: Boolean = false
)

/** Single-row-per-key store for sync bookkeeping, e.g. key "historyId". */
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val key: String,
    val value: String
)
