package com.example.summary

import com.example.classify.EmailMeta
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleSummarizerTest {
    private val s = RuleSummarizer()
    private fun mail(subject: String, snippet: String) = EmailMeta("A", "a@x.com", subject, snippet)

    @Test fun billExtractsAmountAndDueDate() = runTest {
        val r = s.summarize(mail("Invoice", "Your balance of $1,234.50 is due by Nov 15, 2026. Thanks."), "Bill")
        assertTrue(r.text, r.text.contains("Amount: $1,234.50."))
        assertTrue(r.text, r.text.contains("Due: Nov 15, 2026."))
        assertEquals(listOf("Pay bill"), r.actions)
    }

    @Test fun longSnippetIsTrimmedAtWordBoundary() = runTest {
        val long = "word ".repeat(100)
        val r = s.summarize(mail("Hi", long), "Work")
        assertTrue(r.text.length <= 141)
        assertTrue(r.text.endsWith("…"))
        assertEquals(listOf("Reply"), r.actions)
    }

    @Test fun emptySnippetFallsBackToSubject() = runTest {
        assertEquals("Hello there", s.summarize(mail("Hello there", "  "), "Work").text)
    }

    @Test fun newsletterSuggestsUnsubscribe() = runTest {
        assertEquals(listOf("Unsubscribe", "Archive"), s.summarize(mail("News", "stuff"), "Newsletter").actions)
    }
}
