package com.example.classify

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleClassifierTest {
    private val classifier = RuleClassifier()

    private fun mail(
        subject: String, snippet: String = "", sender: String = "Someone", email: String = "someone@example.com",
        labels: List<String> = listOf("INBOX"), headers: Map<String, String> = emptyMap()
    ) = EmailMeta(sender, email, subject, snippet, labels, headers)

    @Test fun promotionsLabelIsPromotionAndClutter() = runTest {
        val r = classifier.classify(
            mail("Big sale", labels = listOf("INBOX", "CATEGORY_PROMOTIONS"), headers = mapOf("list-unsubscribe" to "<mailto:u@x.com>"))
        )
        assertEquals("Promotion", r.category)
        assertTrue(r.isClutter)
        assertTrue(r.isUnsubscribeSuggested)
    }

    @Test fun invoiceIsBillAndNotClutter() = runTest {
        val r = classifier.classify(mail("Your invoice for October", "Amount due $42.10", email = "billing@power.com"))
        assertEquals("Bill", r.category)
        assertFalse(r.isClutter)
    }

    @Test fun billBeatsUpdatesLabel() = runTest {
        val r = classifier.classify(mail("Statement is ready", labels = listOf("INBOX", "CATEGORY_UPDATES")))
        assertEquals("Bill", r.category)
    }

    @Test fun listHeadersMakeNewsletter() = runTest {
        val r = classifier.classify(
            mail("Weekly digest", headers = mapOf("list-id" to "<digest.example.com>", "list-unsubscribe" to "<https://x/u>"))
        )
        assertEquals("Newsletter", r.category)
        assertTrue(r.isClutter)
        assertTrue(r.isUnsubscribeSuggested)
    }

    @Test fun bulkPrecedenceIsNewsletter() = runTest {
        assertEquals("Newsletter", classifier.classify(mail("News", headers = mapOf("precedence" to "bulk"))).category)
    }

    @Test fun socialLabel() = runTest {
        val r = classifier.classify(mail("Anna tagged you", labels = listOf("INBOX", "CATEGORY_SOCIAL")))
        assertEquals("Social", r.category)
        assertTrue(r.isClutter)
    }

    @Test fun personalMailIsWorkAndKept() = runTest {
        val r = classifier.classify(mail("Lunch tomorrow?", "Are you free at noon", labels = listOf("INBOX", "CATEGORY_PERSONAL")))
        assertEquals("Work", r.category)
        assertFalse(r.isClutter)
        assertFalse(r.isUnsubscribeSuggested)
    }

    @Test fun promoWordsWithoutLabelFallToPromotion() = runTest {
        assertEquals("Promotion", classifier.classify(mail("50% off everything today")).category)
    }
}
