package com.example.summary

import com.example.classify.EmailMeta

/** Fallback summarizer for devices without Gemini Nano: trimmed snippet plus amount and due date. */
class RuleSummarizer : Summarizer {

    override suspend fun summarize(email: EmailMeta, category: String): Summary = summarizeNow(email, category)

    fun summarizeNow(email: EmailMeta, category: String): Summary {
        val body = email.snippet.replace(Regex("\\s+"), " ").trim()
        val base = trim(body.ifEmpty { email.subject }, MAX_LEN)
        val text = "${email.subject} ${email.snippet}"
        val amount = AMOUNT.find(text)?.value?.trim()
        val due = DUE.find(text)?.groupValues?.get(1)?.trim()?.trimEnd('.', ',')

        val parts = ArrayList<String>()
        parts += base
        if (category == "Bill") {
            if (amount != null) parts += "Amount: $amount."
            if (due != null) parts += "Due: $due."
        }

        val actions = when (category) {
            "Bill" -> listOf(if (amount != null) "Pay bill" else "Review bill")
            "Newsletter", "Promotion" -> listOf("Unsubscribe", "Archive")
            "Social" -> listOf("Archive")
            else -> listOf("Reply")
        }
        return Summary(parts.joinToString(" "), actions)
    }

    private fun trim(s: String, max: Int): String {
        if (s.length <= max) return s
        val cut = s.substring(0, max)
        val atSpace = cut.lastIndexOf(' ')
        return (if (atSpace > max / 2) cut.substring(0, atSpace) else cut).trimEnd('.', ',', ' ') + "…"
    }

    private companion object {
        const val MAX_LEN = 140
        val AMOUNT = Regex("(?:[$€£₹]|USD|EUR|GBP|INR|Rs\\.?)\\s?\\d[\\d,]*(?:\\.\\d{1,2})?")
        val DUE = Regex(
            "(?i)\\bdue\\s+(?:on|by|date:?)?\\s*" +
                "((?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.?\\s+\\d{1,2}(?:,?\\s+\\d{4})?" +
                "|\\d{1,2}[/-]\\d{1,2}(?:[/-]\\d{2,4})?|today|tomorrow)"
        )
    }
}
