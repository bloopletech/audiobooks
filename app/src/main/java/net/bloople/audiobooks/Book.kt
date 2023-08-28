package net.bloople.audiobooks

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import java.io.File

open class NewBook(
    var path: String,
    var title: String,
    var mtime: Long = 0,
    var size: Long = 0,
    var lastOpenedAt: Long = 0,
    var openedCount: Int = 0,
    var lastReadPosition: Long = 0,
    var starred: Boolean = false
) {
    val file
        get() = File(path)

    val uri
        get() = Uri.fromFile(file).toString()

    fun toValues(): ContentValues {
        return ContentValues().apply {
            put("path", path)
            put("title", title)
            put("mtime", mtime)
            put("size", size)
            put("last_opened_at", lastOpenedAt)
            put("opened_count", openedCount)
            put("last_read_position", lastReadPosition)
            put("starred", if (starred) 1 else 0)
        }
    }

    open fun save(context: Context): Book {
        val db = DatabaseHelper.instance(context)
        val id = db.insert("books", null, toValues())
        return Book(id, path, title, mtime, size, lastOpenedAt, openedCount, lastReadPosition, starred)
    }
}

class Book(
    private val _id: Long,
    path: String,
    title: String,
    mtime: Long,
    size: Long,
    lastOpenedAt: Long,
    openedCount: Int,
    lastReadPosition: Long,
    starred: Boolean
): NewBook(path, title, mtime, size, lastOpenedAt, openedCount, lastReadPosition, starred) {
    constructor(result: Cursor) : this(
        result["_id"],
        result["path"],
        result["title"],
        result["mtime"],
        result["size"],
        result["last_opened_at"],
        result["opened_count"],
        result["last_read_position"],
        result["starred"]
    )

    override fun save(context: Context): Book {
        val db = DatabaseHelper.instance(context)
        db.update("books", toValues(), "_id=?", arrayOf(_id.toString()))
        return this
    }

    fun edit(context: Context, block: Book.() -> Unit): Book {
        return apply(block).apply { save(context) }
    }

    fun destroy(context: Context) {
        val db = DatabaseHelper.instance(context)
        db.delete("books", "_id=?", arrayOf(_id.toString()))
    }

    companion object {
        fun edit(context: Context, id: Long, block: Book.() -> Unit): Book {
            return find(context, id).edit(context, block)
        }

        @JvmStatic
        fun find(context: Context, id: Long): Book {
            val db = DatabaseHelper.instance(context)
            db.rawQuery("SELECT * FROM books WHERE _id=?", arrayOf(id.toString())).use {
                it.moveToFirst()
                return if (it.count > 0) Book(it) else throw NoSuchElementException("Book with id $id not found")
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

        fun findAll(context: Context, block: (Cursor) -> Unit) {
            val db = DatabaseHelper.instance(context)
            db.query("books", null, null, null, null, null, null).use(block)
        }

        fun idFrom(intent: Intent?): Long {
            return intent!!.getLongExtra("_id", -1).also {
                if(it == -1L) throw IllegalArgumentException("Intent missing extra _id of type long")
            }
        }

        fun idTo(intent: Intent, id: Long): Intent {
            return intent.apply { putExtra("_id", id) }
        }
    }
}
