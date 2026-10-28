package com.example.outbox

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.EmailStore
import com.example.data.ServerEmail
import com.example.gmail.*
import com.example.sync.FakeGmailApi
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OutboxProcessorTest {
    private lateinit var db: AppDatabase
    private val batches = mutableListOf<BatchModifyRequest>()
    private val trashed = mutableListOf<String>()
    private var fail: HttpException? = null

    private val api = object : GmailApi by FakeGmailApi() {
        override suspend fun batchModify(body: BatchModifyRequest) { fail?.let { throw it }; batches += body }
        override suspend fun trash(id: String): GmailMessage { fail?.let { throw it }; trashed += id; return GmailMessage(id) }
    }

    @Before fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java)
            .allowMainThreadQueries().build()
        EmailStore(db).merge(listOf("a", "b", "c").map { ServerEmail(it, "t", "S", "s@x", "sub", "sn", 1, listOf("INBOX", "UNREAD")) })
        Unit
    }

    @After fun tearDown() = db.close()

    private fun http(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())))

    @Test fun archiveAndReadBatchToGmailThenClearPending() = runBlocking {
        val dao = db.emailDao()
        dao.archiveEmailsForBulk(listOf("a", "b"))
        dao.setReadAndQueue(listOf("c"), true, PendingAction.MARK_READ)
        assertEquals(3, OutboxProcessor(api, db).flush())
        assertTrue(batches.any { it.ids == listOf("a", "b") && it.removeLabelIds == listOf("INBOX") })
        assertTrue(batches.any { it.ids == listOf("c") && it.removeLabelIds == listOf("UNREAD") })
        assertTrue(dao.getPending().isEmpty())
        assertTrue(dao.getEmailById("a")!!.isArchived)
    }

    @Test fun trashCallsGmailAndRemovesRow() = runBlocking {
        db.emailDao().trashEmails(listOf("a"))
        OutboxProcessor(api, db).flush()
        assertEquals(listOf("a"), trashed)
        assertNull(db.emailDao().getEmailById("a"))
    }

    @Test fun failureKeepsActionQueuedForRetry() = runBlocking {
        db.emailDao().archiveEmail("a")
        fail = http(500)
        try { OutboxProcessor(api, db).flush(); fail("expected") } catch (e: HttpException) {}
        assertEquals("ARCHIVE", db.emailDao().getEmailById("a")!!.pendingAction)
        fail = null
        assertEquals(1, OutboxProcessor(api, db).flush())
        assertNull(db.emailDao().getEmailById("a")!!.pendingAction)
    }

    @Test fun alreadyGoneMessageCountsAsDone() = runBlocking {
        db.emailDao().trashEmails(listOf("a"))
        fail = http(404)
        OutboxProcessor(api, db).flush()
        assertNull(db.emailDao().getEmailById("a"))
    }

    @Test fun syncDoesNotUnarchiveQueuedRows() = runBlocking {
        db.emailDao().archiveEmail("a")
        EmailStore(db).merge(listOf(ServerEmail("a", "t", "S", "s@x", "new", "sn", 1, listOf("INBOX"))))
        assertTrue(db.emailDao().getEmailById("a")!!.isArchived)
        assertEquals("ARCHIVE", db.emailDao().getEmailById("a")!!.pendingAction)
    }
}
