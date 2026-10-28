package com.example.sync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.classify.RuleClassifier
import com.example.data.AppDatabase
import com.example.gmail.*
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response

class FakeGmailApi : GmailApi {
    val messages = linkedMapOf<String, GmailMessage>()
    var historyId = 100L
    var history: List<HistoryRecord> = emptyList()
    var historyExpired = false
    var historyCalls = 0

    fun put(id: String, subject: String, labels: List<String> = listOf("INBOX", "UNREAD"), extra: List<Header> = emptyList()) {
        messages[id] = GmailMessage(
            id, "t$id", labels, "snippet $id", "1000",
            payload = Payload(listOf(Header("From", "Bob <bob@x.com>"), Header("Subject", subject)) + extra)
        )
    }

    private fun notFound() = HttpException(Response.error<Any>(404, "".toResponseBody("application/json".toMediaType())))

    override suspend fun getProfile() = Profile("me@x.com", historyId.toString())
    override suspend fun listMessages(labelIds: String, maxResults: Int, pageToken: String?) =
        MessageListResponse(messages.values.filter { labelIds in it.labelIds.orEmpty() }.map { MessageRef(it.id) }.take(maxResults))
    override suspend fun getMessage(id: String, format: String, metadataHeaders: List<String>) =
        messages[id] ?: throw notFound()
    override suspend fun listHistory(startHistoryId: String, labelId: String, maxResults: Int, pageToken: String?): HistoryResponse {
        historyCalls++
        if (historyExpired) throw notFound()
        return HistoryResponse(history, null, historyId.toString())
    }
    override suspend fun batchModify(body: BatchModifyRequest) {}
    override suspend fun trash(id: String) = messages.getValue(id)
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SyncEngineTest {
    private lateinit var db: AppDatabase
    private lateinit var api: FakeGmailApi
    private lateinit var engine: SyncEngine

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java)
            .allowMainThreadQueries().build()
        api = FakeGmailApi()
        engine = SyncEngine(api, db, RuleClassifier())
    }

    @After fun tearDown() = db.close()

    @Test fun firstSyncFetchesInboxClassifiesAndStoresHistoryId() = runBlocking {
        api.put("a", "Your invoice")
        api.put("b", "Hello", extra = listOf(Header("List-Unsubscribe", "<mailto:x>")))
        val r = engine.sync()
        assertTrue(r.fullResync); assertEquals(2, r.added)
        assertEquals("Bill", db.emailDao().getEmailById("a")!!.category)
        val b = db.emailDao().getEmailById("b")!!
        assertEquals("Newsletter", b.category); assertTrue(b.isUnsubscribeSuggested)
        assertEquals("Bob", b.sender); assertEquals("bob@x.com", b.senderEmail)
        assertEquals("100", db.syncStateDao().get("historyId"))
    }

    @Test fun nonInboxMessagesAreNotFetched() = runBlocking {
        api.put("a", "x"); api.put("sent", "y", labels = listOf("SENT"))
        engine.sync()
        assertNull(db.emailDao().getEmailById("sent"))
    }

    @Test fun incrementalAddsNewAndKeepsLocalState() = runBlocking {
        api.put("a", "old")
        engine.sync()
        db.emailDao().updateReadStatus("a", true)
        db.emailDao().updateCategory("a", "Social")

        api.put("a", "renamed", labels = listOf("INBOX"))
        api.put("c", "fresh")
        api.historyId = 120
        api.history = listOf(
            HistoryRecord(messagesAdded = listOf(HistoryMessage(MessageRef("c")))),
            HistoryRecord(labelsRemoved = listOf(HistoryLabelChange(MessageRef("a"), listOf("UNREAD"))))
        )
        val r = engine.sync()
        assertFalse(r.fullResync); assertEquals(1, r.added); assertEquals(1, r.updated)
        val a = db.emailDao().getEmailById("a")!!
        assertEquals("renamed", a.subject); assertTrue(a.isRead); assertEquals("Social", a.category)
        assertNotNull(db.emailDao().getEmailById("c"))
        assertEquals("120", db.syncStateDao().get("historyId"))
    }

    @Test fun serverSideArchiveAndDeleteAreApplied() = runBlocking {
        api.put("a", "x"); api.put("b", "y")
        engine.sync()
        api.put("a", "x", labels = listOf("CATEGORY_UPDATES"))
        api.messages.remove("b")
        api.history = listOf(
            HistoryRecord(labelsRemoved = listOf(HistoryLabelChange(MessageRef("a"), listOf("INBOX")))),
            HistoryRecord(messagesDeleted = listOf(HistoryMessage(MessageRef("b"))))
        )
        val r = engine.sync()
        assertEquals(1, r.removed)
        assertTrue(db.emailDao().getEmailById("a")!!.isArchived)
        assertNull(db.emailDao().getEmailById("b"))
    }

    @Test fun expiredHistoryFallsBackToFullResyncAndArchivesStale() = runBlocking {
        api.put("a", "x"); api.put("b", "y")
        engine.sync()
        api.messages.remove("b")
        api.put("n", "new")
        api.historyExpired = true
        val r = engine.sync()
        assertTrue(r.fullResync)
        assertTrue(db.emailDao().getEmailById("b")!!.isArchived)
        assertFalse(db.emailDao().getEmailById("a")!!.isArchived)
        assertNotNull(db.emailDao().getEmailById("n"))
    }

    @Test fun serverErrorsPropagateInsteadOfFakeData() = runBlocking {
        api.put("a", "x"); engine.sync()
        val failing = object : GmailApi by api {
            override suspend fun listHistory(startHistoryId: String, labelId: String, maxResults: Int, pageToken: String?): HistoryResponse =
                throw HttpException(Response.error<Any>(500, "".toResponseBody("application/json".toMediaType())))
        }
        try { SyncEngine(failing, db, RuleClassifier()).sync(); fail() } catch (e: HttpException) { assertEquals(500, e.code()) }
    }
}
