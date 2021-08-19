package net.bloople.audiobooks

import android.content.Context
import android.database.sqlite.SQLiteDatabase

internal object DatabaseHelper {
    private const val DB_NAME = "books"
    private lateinit var database: SQLiteDatabase
    private fun obtainDatabase(context: Context): SQLiteDatabase {
        val db = context.applicationContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS books ( " +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "path TEXT, " +
                    "title TEXT, " +
                    "mtime INT DEFAULT 0, " +
                    "size INTEGER DEFAULT 0, " +
                    "last_opened_at INTEGER DEFAULT 0, " +
                    "last_read_position INTEGER DEFAULT 0, " +
                    "starred INTEGER DEFAULT 0" +
                    ")"
        )
        return db
    }

    @JvmStatic
    fun instance(context: Context): SQLiteDatabase {
        if (!::database.isInitialized) {
            database = obtainDatabase(context)
        }
        return database
    }

    @JvmStatic
    fun deleteDatabase(context: Context) {
        context.applicationContext.deleteDatabase(DB_NAME)
        database = obtainDatabase(context)
    }
}