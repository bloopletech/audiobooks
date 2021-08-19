package net.bloople.audiobooks

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import java.io.File

internal class Book {
    var _id = -1L
    var path: String? = null
    var title: String? = null
    var mtime: Long = 0
    var size: Long = 0
    var lastOpenedAt: Long = 0
    var lastReadPosition: Long = 0
    var starred = false

    constructor()
    constructor(result: Cursor) {
        _id = result.getLong(result.getColumnIndex("_id"))
        path = result.getString(result.getColumnIndex("path"))
        title = result.getString(result.getColumnIndex("title"))
        mtime = result.getLong(result.getColumnIndex("mtime"))
        size = result.getLong(result.getColumnIndex("size"))
        lastOpenedAt = result.getLong(result.getColumnIndex("last_opened_at"))
        lastReadPosition = result.getLong(result.getColumnIndex("last_read_position"))
        starred = result.getInt(result.getColumnIndex("starred")) == 1
    }

    val uri: String
        get() {
            return Uri.fromFile(File(path)).toString()
        }

    fun save(context: Context) {
        val values = ContentValues().apply {
            put("path", path)
            put("title", title)
            put("mtime", mtime)
            put("size", size)
            put("last_opened_at", lastOpenedAt)
            put("last_read_position", lastReadPosition)
            put("starred", if (starred) 1 else 0)
        }
        val db = DatabaseHelper.instance(context)
        if (_id == -1L) {
            _id = db.insert("books", null, values)
        } else {
            db.update("books", values, "_id=?", arrayOf(_id.toString()))
        }
    }

    inline fun <R> edit(context: Context, block: Book.() -> R): R {
        return block(this).also { save(context) }
    }

    fun destroy(context: Context) {
        val db = DatabaseHelper.instance(context)
        db.delete("books", "_id=?", arrayOf(_id.toString()))
    }

    companion object {
        inline fun <R> edit(context: Context, id: Long, block: Book.() -> R): R {
            val book = find(context, id)
            return block(book).also { book.save(context) }
        }

        inline fun <R> editOrNull(context: Context, id: Long, block: Book.() -> R?): R? {
            val book = findByIdOrNull(context, id) ?: return null
            return block(book).also { book.save(context) }
        }

        fun find(context: Context, id: Long): Book {
            return findById(context, id)
        }

        fun findById(context: Context, id: Long): Book {
            val db = DatabaseHelper.instance(context)
            db.rawQuery("SELECT * FROM books WHERE _id=?", arrayOf(id.toString())).use {
                it.moveToFirst()
                return if (it.count > 0) Book(it) else throw NoSuchElementException("Book with id $id not found")
            }
        }

        fun findOrNull(context: Context, id: Long): Book? {
            return findByIdOrNull(context, id)
        }

        @JvmStatic
        fun findByIdOrNull(context: Context, id: Long): Book? {
            val db = DatabaseHelper.instance(context)
            db.rawQuery("SELECT * FROM books WHERE _id=?", arrayOf(id.toString())).use {
                it.moveToFirst()
                return if (it.count > 0) Book(it) else null
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
    }
}

