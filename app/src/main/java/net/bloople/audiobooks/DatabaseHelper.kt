package net.bloople.audiobooks

import android.content.Context
import android.database.sqlite.SQLiteDatabase

internal object DatabaseHelper {
    private const val DB_NAME = "books"
    private lateinit var database: SQLiteDatabase
    private fun obtainDatabase(context: Context): SQLiteDatabase {
        val db = context.applicationContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null)
        loadSchema(db)
        return db
    }

    private fun loadSchema(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS books ( " +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "path TEXT, " +
            "title TEXT, " +
            "mtime INT DEFAULT 0, " +
            "size INTEGER DEFAULT 0, " +
            "last_opened_at INTEGER DEFAULT 0, " +
            "last_read_position INTEGER DEFAULT 0, " +
            "starred INTEGER DEFAULT 0" +
            ")")

        if (!hasColumn(db, "books", "opened_count")) {
            db.execSQL("ALTER TABLE books ADD COLUMN opened_count INTEGER")
            db.execSQL("UPDATE books SET opened_count=0")
        }
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

    private fun hasColumn(db: SQLiteDatabase, tableName: String, columnName: String): Boolean {
        db.rawQuery("PRAGMA table_info($tableName)", null).use {
            while (it.moveToNext()) {
                if (it.getString(it.getColumnIndex("name")).equals(columnName)) {
                    return true
                }
            }
        }
        return false
    }
}