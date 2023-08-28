package net.bloople.audiobooks

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

internal object DatabaseHelper {
    private const val DB_NAME = "books"
    private lateinit var database: SQLiteDatabase

    private fun obtainDatabase(): SQLiteDatabase {
        val db = AudiobooksApplication.context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null)
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
    @Synchronized
    fun instance(): SQLiteDatabase {
        if (!::database.isInitialized) {
            database = obtainDatabase()
        }
        return database
    }

    @JvmStatic
    @Synchronized
    fun deleteDatabase() {
        AudiobooksApplication.context.deleteDatabase(DB_NAME)
        database = obtainDatabase()
    }

    @JvmStatic
    @Synchronized
    fun exportDatabase(outputStream: OutputStream) {
        val path = instance().use { it.path }
        database = obtainDatabase()

        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(path)
            val buffer = ByteArray(1024)
            var length: Int
            while(inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
        }
        finally {
            inputStream?.close()
            outputStream.close()
        }
    }

    @JvmStatic
    @Synchronized
    fun importDatabase(inputStream: InputStream) {
        val path = instance().use { it.path }
        database = obtainDatabase()

        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(path)
            val buffer = ByteArray(1024)
            var length: Int
            while(inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
        }
        finally {
            inputStream.close()
            outputStream?.close()
        }
    }

    private fun hasColumn(db: SQLiteDatabase, tableName: String, columnName: String): Boolean {
        db.rawQuery("PRAGMA table_info($tableName)", null).use {
            while (it.moveToNext()) {
                if (it.getString(it.getColumnIndexOrThrow("name")).equals(columnName)) {
                    return true
                }
            }
        }
        return false
    }
}