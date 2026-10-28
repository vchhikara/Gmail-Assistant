package com.example.gmail

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageRef(val id: String, val threadId: String? = null)

@JsonClass(generateAdapter = true)
data class MessageListResponse(
    val messages: List<MessageRef>? = null,
    val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class Header(val name: String? = null, val value: String? = null)

@JsonClass(generateAdapter = true)
data class Payload(val headers: List<Header>? = null)

@JsonClass(generateAdapter = true)
data class GmailMessage(
    val id: String,
    val threadId: String? = null,
    val labelIds: List<String>? = null,
    val snippet: String? = null,
    val internalDate: String? = null,
    val historyId: String? = null,
    val payload: Payload? = null
)

@JsonClass(generateAdapter = true)
data class Profile(val emailAddress: String? = null, val historyId: String)

@JsonClass(generateAdapter = true)
data class HistoryMessage(val message: MessageRef? = null)

@JsonClass(generateAdapter = true)
data class HistoryLabelChange(val message: MessageRef? = null, val labelIds: List<String>? = null)

@JsonClass(generateAdapter = true)
data class HistoryRecord(
    val id: String? = null,
    val messagesAdded: List<HistoryMessage>? = null,
    val messagesDeleted: List<HistoryMessage>? = null,
    val labelsAdded: List<HistoryLabelChange>? = null,
    val labelsRemoved: List<HistoryLabelChange>? = null
)

@JsonClass(generateAdapter = true)
data class HistoryResponse(
    val history: List<HistoryRecord>? = null,
    val nextPageToken: String? = null,
    val historyId: String? = null
)

@JsonClass(generateAdapter = true)
data class BatchModifyRequest(
    val ids: List<String>,
    val addLabelIds: List<String> = emptyList(),
    val removeLabelIds: List<String> = emptyList()
)
