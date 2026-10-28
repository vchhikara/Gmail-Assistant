package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [EmailEntity::class, SyncStateEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emailDao(): EmailDao
    abstract fun syncStateDao(): SyncStateDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE emails ADD COLUMN labelIds TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE emails ADD COLUMN isUnread INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE emails ADD COLUMN pendingAction TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE emails ADD COLUMN categoryManual INTEGER NOT NULL DEFAULT 0")
                // Rows from v1 were already unread/read per the user's state; carry that over.
                db.execSQL("UPDATE emails SET isUnread = CASE WHEN isRead = 0 THEN 1 ELSE 0 END")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS sync_state (" +
                        "`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_gmail_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
