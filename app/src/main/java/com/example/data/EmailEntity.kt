package com.example.data

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
    val suggestedActions: String? = null // comma-separated or custom actions
)
