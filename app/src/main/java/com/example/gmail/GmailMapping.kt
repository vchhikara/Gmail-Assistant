package com.example.gmail

import com.example.classify.EmailMeta
import com.example.data.ServerEmail

private val ANGLE = Regex("<([^>]+)>")

/** "Name <a@b.com>" -> (Name, a@b.com); "a@b.com" -> (a@b.com, a@b.com). */
fun parseFrom(raw: String?): Pair<String, String> {
    if (raw.isNullOrBlank()) return "Unknown Sender" to ""
    val email = ANGLE.find(raw)?.groupValues?.get(1)?.trim() ?: raw.trim()
    val name = raw.substringBefore('<').trim().trim('"').ifEmpty { email }
    return name to email
}

fun GmailMessage.headerMap(): Map<String, String> =
    payload?.headers.orEmpty()
        .mapNotNull { h -> h.name?.let { it.lowercase() to (h.value ?: "") } }
        .toMap()

fun GmailMessage.toServerEmail(): ServerEmail {
    val h = headerMap()
    val (name, email) = parseFrom(h["from"])
    return ServerEmail(
        id = id,
        threadId = threadId ?: id,
        sender = name,
        senderEmail = email,
        subject = h["subject"]?.ifBlank { null } ?: "(No Subject)",
        snippet = snippet.orEmpty(),
        timestamp = internalDate?.toLongOrNull() ?: 0L,
        labelIds = labelIds.orEmpty(),
        headers = h
    )
}

fun ServerEmail.toMeta() = EmailMeta(sender, senderEmail, subject, snippet, labelIds, headers)
