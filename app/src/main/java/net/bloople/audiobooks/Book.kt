package net.bloople.audiobooks

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import java.io.File

internal class Book {
    var _id: Long? = null
    var path: String? = null
    var title: String? = null
    var mtime: Long = 0
    var size: Long = 0
    var lastOpenedAt: Long = 0
    var openedCount: Int = 0
    var lastReadPosition: Long = 0
    var starred = false

    constructor()
    constructor(result: Cursor) {
        _id = result["_id"]
        path = result["path"]
        title = result["title"]
        mtime = result["mtime"]
        size = result["size"]
        lastOpenedAt = result["last_opened_at"]
        openedCount = result["opened_count"]
        lastReadPosition = result["last_read_position"]
        starred = 1 == result["starred"]
    }

    val uri: String
        get() {
            return Uri.fromFile(File(path)).toString()
        }

    val isNew get() = this._id == null

    fun save(context: Context) {
        val values = ContentValues().apply {
            put("path", path)
            put("title", title)
            put("mtime", mtime)
            put("size", size)
            put("last_opened_at", lastOpenedAt)
            put("opened_count", openedCount)
            put("last_read_position", lastReadPosition)
            put("starred", if (starred) 1 else 0)
        }
        val db = DatabaseHelper.instance(context)
        if (_id != null) {
            db.update("books", values, "_id=?", arrayOf(_id.toString()))
        }
        else {
            _id = db.insert("books", null, values)
        }
    }

    inline fun <R> edit(context: Context, block: Book.() -> R): R {
        return block(this).also { save(context) }
    }

    fun destroy(context: Context) {
        val db = DatabaseHelper.instance(context)
        db.delete("books", "_id=?", arrayOf(_id.toString()))
    }

    fun idTo(intent: Intent): Intent {
        return intent.apply { putExtra("_id", _id) }
    }

    companion object {
        inline fun <R> edit(context: Context, id: Long, block: Book.() -> R): R {
            val book = find(context, id)
            return block(book).also { book.save(context) }
        }

        inline fun <R> editOrNull(context: Context, id: Long, block: Book.() -> R?): R? {
            val book = findOrNull(context, id) ?: return null
            return block(book).also { book.save(context) }
        }

        @JvmStatic
        fun find(context: Context, id: Long): Book {
            val db = DatabaseHelper.instance(context)
            db.rawQuery("SELECT * FROM books WHERE _id=?", arrayOf(id.toString())).use {
                it.moveToFirst()
                return if (it.count > 0) Book(it) else throw NoSuchElementException("Book with id $id not found")
            }
        }

        fun findOrNull(context: Context, id: Long): Book? {
            return try {
                find(context, id)
            } catch(e: NoSuchElementException) {
                null
            }
        }

        @JvmStatic
        fun findByPathOrNull(context: Context, path: String): Book? {
            val db = DatabaseHelper.instance(context)
            db.rawQuery("SELECT * FROM books WHERE path=?", arrayOf(path)).use {
                it.moveToFirst()
                return if (it.count > 0) Book(it) else null
            }
        }

        fun idFrom(intent: Intent?): Long {
            return intent!!.getLongExtra("_id", -1).also {
                if(it == -1L) throw IllegalArgumentException("Intent missing extra _id of type long")
            }
        }

        @JvmStatic
        fun idTo(intent: Intent, id: Long): Intent {
            return intent.apply { putExtra("_id", id) }
        }
    }
}
