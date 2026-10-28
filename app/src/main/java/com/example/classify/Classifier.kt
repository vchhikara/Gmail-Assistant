package com.example.classify

/** What a classifier or summarizer sees of one message. Header keys are lower-case. */
data class EmailMeta(
    val sender: String,
    val senderEmail: String,
    val subject: String,
    val snippet: String,
    val labelIds: List<String> = emptyList(),
    val headers: Map<String, String> = emptyMap()
)

data class Classification(
    val category: String,
    val isClutter: Boolean,
    val isUnsubscribeSuggested: Boolean
)

interface Classifier {
    suspend fun classify(email: EmailMeta): Classification
}
