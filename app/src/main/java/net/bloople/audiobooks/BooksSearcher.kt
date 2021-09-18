package net.bloople.audiobooks

import android.content.Context
import android.database.Cursor
import net.bloople.audiobooks.DatabaseHelper.instance
import java.lang.IllegalStateException

class BooksSearcher internal constructor() {
    private var searchText = ""
    var sortMethod = SORT_AGE
    var sortDirectionAsc = false
    fun setSearchText(inSearchText: String) {
        searchText = inSearchText
    }

    fun flipSortDirection() {
        sortDirectionAsc = !sortDirectionAsc
    }

    fun description(): String {
        return "Sorted by " + sortMethodDescription().toLowerCase() + " " + sortDirectionDescription().toLowerCase()
    }

    fun sortMethodDescription(): String {
        return when (sortMethod) {
            SORT_ALPHABETIC -> "Title"
            SORT_AGE -> "Published Date"
            SORT_SIZE -> "Size"
            SORT_LAST_OPENED -> "Last Opened At"
            SORT_OPENED_COUNT -> "Opened Count"
            SORT_STARRED -> "Starred"
            else -> throw IllegalStateException("sort_method not in valid range")
        }
    }

    private fun sortDirectionDescription(): String {
        return if (sortDirectionAsc) "Ascending" else "Descending"
    }

    private fun orderBy(): String {
        var orderBy = ""
        when (sortMethod) {
            SORT_ALPHABETIC -> orderBy += "title"
            SORT_AGE -> orderBy += "mtime"
            SORT_SIZE -> orderBy += "size"
            SORT_LAST_OPENED -> orderBy += "last_opened_at"
            SORT_STARRED -> orderBy += "starred"
            SORT_OPENED_COUNT -> orderBy += "opened_count"
        }
        orderBy += if (sortDirectionAsc) " ASC" else " DESC"
        orderBy += ", title ASC"
        return orderBy
    }

    fun search(context: Context?): Cursor {
        val db = instance(context!!)
        val cursor: Cursor = if (searchText != "") {
            db.query("books", null, "title LIKE ?", arrayOf("%$searchText%"), null, null, orderBy())
        } else {
            db.query("books", null, null, null, null, null, orderBy())
        }
        cursor.moveToFirst()
        return cursor
    }

    companion object {
        const val SORT_ALPHABETIC = 0
        const val SORT_AGE = 1
        const val SORT_SIZE = 2
        const val SORT_LAST_OPENED = 3
        const val SORT_STARRED = 4
        const val SORT_OPENED_COUNT = 5
    }
}