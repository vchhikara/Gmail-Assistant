package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EmailStoreTest {
    private lateinit var db: AppDatabase
    private lateinit var store: EmailStore

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java)
            .allowMainThreadQueries().build()
        store = EmailStore(db)
    }

    @After fun tearDown() = db.close()

    private fun server(id: String, subject: String = "s", labels: List<String> = listOf("INBOX", "UNREAD")) =
        ServerEmail(id, "t$id", "Sender", "s@x.com", subject, "snip", 1000L, labels)

    @Test fun newMessagesAreInsertedAndReported() = runBlocking {
        val ids = store.merge(listOf(server("a"), server("b")))
        assertEquals(listOf("a", "b"), ids)
        val a = db.emailDao().getEmailById("a")!!
        assertTrue(a.isUnread)
        assertEquals("INBOX,UNREAD", a.labelIds)
    }

    @Test fun mergeKeepsLocalUserState() = runBlocking {
        val dao = db.emailDao()
        store.merge(listOf(server("a", "old")))
        dao.updateReadStatus("a", true)
        dao.archiveEmail("a")
        dao.updateCategory("a", "Bill")
        dao.updateSummaryAndActions("a", "my summary", "Pay bill")
        dao.setPendingAction(listOf("a"), "ARCHIVE")

        val inserted = store.merge(listOf(server("a", "new subject")))

        assertTrue(inserted.isEmpty())
        val a = dao.getEmailById("a")!!
        assertEquals("new subject", a.subject)
        assertTrue(a.isRead)
        assertTrue(a.isArchived)
        assertEquals("Bill", a.category)
        assertTrue(a.categoryManual)
        assertEquals("my summary", a.summary)
        assertEquals("ARCHIVE", a.pendingAction)
    }

    @Test fun classificationSkipsManualCategory() = runBlocking {
        val dao = db.emailDao()
        store.merge(listOf(server("a"), server("b")))
        dao.updateCategory("a", "Social")
        dao.updateClassification("a", "Promotion", true, true)
        dao.updateClassification("b", "Promotion", true, true)
        assertEquals("Social", dao.getEmailById("a")!!.category)
        assertEquals("Promotion", dao.getEmailById("b")!!.category)
    }

    @Test fun syncStateRoundTrip() = runBlocking {
        val d = db.syncStateDao()
        assertEquals(null, d.get("historyId"))
        d.put(SyncStateEntity("historyId", "123"))
        d.put(SyncStateEntity("historyId", "456"))
        assertEquals("456", d.get("historyId"))
        d.remove("historyId")
        assertEquals(null, d.get("historyId"))
        assertNotNull(db.openHelper)
    }
}
