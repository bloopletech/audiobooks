package net.bloople.audiobooks

import android.database.Cursor
import androidx.recyclerview.widget.RecyclerView

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 ARNAUD FRUGIER
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

abstract class CursorRecyclerAdapter<VH : RecyclerView.ViewHolder>(c: Cursor?) : RecyclerView.Adapter<VH>() {
    private var cursor: Cursor? = null
    private var mRowIDColumn = 0

    fun init(c: Cursor?) {
        cursor = c
        mRowIDColumn = c?.getColumnIndexOrThrow("_id") ?: -1
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        check(cursor != null) { "this should only be called when the cursor is valid" }
        check(cursor!!.moveToPosition(position)) { "couldn't move cursor to position $position" }
        onBindViewHolder(holder, cursor!!)
    }

    abstract fun onBindViewHolder(holder: VH, cursor: Cursor)

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }

    override fun getItemId(position: Int): Long {
        return if(hasStableIds() && cursor != null) {
            if(cursor!!.moveToPosition(position)) {
                cursor!!.getLong(mRowIDColumn)
            }
            else {
                RecyclerView.NO_ID
            }
        }
        else {
            RecyclerView.NO_ID
        }
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
    fun changeCursor(cursor: Cursor?) {
        val old = swapCursor(cursor)
        old?.close()
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * [.changeCursor], the returned old Cursor is *not*
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null if there wasa not one.
     * If the given new Cursor is the same instance is the previously set
     * Cursor, null is also returned.
     */
    fun swapCursor(newCursor: Cursor?): Cursor? {
        if(newCursor === cursor) {
            return null
        }

        val oldCursor = cursor
        val itemCount = itemCount
        cursor = newCursor
        if(newCursor != null) {
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id")
            // notify the observers about the new cursor
            notifyDataSetChanged()
        }
        else {
            mRowIDColumn = -1
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, itemCount)
        }
        return oldCursor
    }

    /**
     *
     * Converts the cursor into a CharSequence. Subclasses should override this
     * method to convert their results. The default implementation returns an
     * empty String for null values or the default String representation of
     * the value.
     *
     * @param cursor the cursor to convert to a CharSequence
     * @return a CharSequence representing the value
     */
    fun convertToString(cursor: Cursor?): CharSequence {
        return cursor?.toString() ?: ""
    }

    init {
        init(c)
    }
}