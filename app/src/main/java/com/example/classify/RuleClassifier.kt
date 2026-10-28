package com.example.classify

/** On-device rules: Gmail category labels and list headers first, then sender and subject patterns. */
class RuleClassifier : Classifier {

    override suspend fun classify(email: EmailMeta): Classification = classifyNow(email)

    fun classifyNow(email: EmailMeta): Classification {
        val labels = email.labelIds.toSet()
        val hasUnsubscribeHeader = email.headers["list-unsubscribe"]?.isNotBlank() == true
        val isBulk = email.headers["precedence"]?.lowercase() in setOf("bulk", "list", "junk") ||
            email.headers["list-id"]?.isNotBlank() == true
        val text = "${email.subject} ${email.snippet}".lowercase()
        val sender = "${email.sender} ${email.senderEmail}".lowercase()

        val isBill = BILL_PATTERN.containsMatchIn(text) ||
            BILL_SENDER.containsMatchIn(sender)

        val category = when {
            isBill && "CATEGORY_PROMOTIONS" !in labels -> "Bill"
            "CATEGORY_PROMOTIONS" in labels -> "Promotion"
            "CATEGORY_SOCIAL" in labels || "CATEGORY_FORUMS" in labels -> "Social"
            hasUnsubscribeHeader || isBulk || "CATEGORY_UPDATES" in labels -> "Newsletter"
            PROMO_PATTERN.containsMatchIn(text) -> "Promotion"
            else -> "Work"
        }

        val clutter = category == "Promotion" || (category == "Newsletter" && (hasUnsubscribeHeader || isBulk)) ||
            (category == "Social" && "CATEGORY_SOCIAL" in labels && "IMPORTANT" !in labels)
        val unsub = hasUnsubscribeHeader && (category == "Promotion" || category == "Newsletter")
        return Classification(category, clutter, unsub)
    }

    private companion object {
        val BILL_PATTERN = Regex(
            "\\b(invoice|your bill|bill is ready|statement is ready|payment due|amount due|due date|" +
                "autopay|past due|overdue|payment receipt|your receipt)\\b"
        )
        val BILL_SENDER = Regex("\\b(billing|invoices?|payments?)@")
        val PROMO_PATTERN = Regex("\\b(\\d{1,2}% off|sale|limited time|exclusive offer|coupon|deal)\\b")
    }
}
