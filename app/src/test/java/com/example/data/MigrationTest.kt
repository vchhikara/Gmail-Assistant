package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MigrationTest {
    @Test fun v1RowsSurviveMigrationWithUserState() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-test.db"
        context.deleteDatabase(name)

        val v1 = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE emails (id TEXT NOT NULL, threadId TEXT NOT NULL, sender TEXT NOT NULL, " +
                                "senderEmail TEXT NOT NULL, subject TEXT NOT NULL, snippet TEXT NOT NULL, " +
                                "timestamp INTEGER NOT NULL, isRead INTEGER NOT NULL, isArchived INTEGER NOT NULL, " +
                                "category TEXT NOT NULL, isUnsubscribeSuggested INTEGER NOT NULL, " +
                                "isClutter INTEGER NOT NULL, summary TEXT, suggestedActions TEXT, PRIMARY KEY(id))"
                        )
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, o: Int, n: Int) {}
                }).build()
        )
        v1.writableDatabase.apply {
            execSQL("INSERT INTO emails VALUES ('a','t','S','s@x.com','Sub','snip',5,1,0,'Bill',0,0,'sum',NULL)")
            execSQL("INSERT INTO emails VALUES ('b','t','S','s@x.com','Sub','snip',6,0,1,'Work',0,0,NULL,NULL)")
            close()
        }

        val db = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(AppDatabase.MIGRATION_1_2).allowMainThreadQueries().build()
        val a = db.emailDao().getEmailById("a")!!
        val b = db.emailDao().getEmailById("b")!!
        assertTrue(a.isRead); assertFalse(a.isUnread)
        assertFalse(b.isRead); assertTrue(b.isUnread); assertTrue(b.isArchived)
        assertEquals("sum", a.summary)
        assertEquals("", a.labelIds)
        assertEquals(null, a.pendingAction)
        assertEquals(null, db.syncStateDao().get("historyId"))
        db.close()
    }
}
