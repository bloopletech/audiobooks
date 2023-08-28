package net.bloople.audiobooks

import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Formatter
import java.util.Locale

class BooksAdapter(cursor: Cursor?) : CursorRecyclerAdapter<BooksAdapter.ViewHolder>(cursor) {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var titleView: TextView
        var sizeView: TextView
        var ageView: TextView?
        var lastOpenedView: TextView
        var openedCountView: TextView?
        var starView: ImageButton

        init {
            view.setOnClickListener { v: View ->
                val context = v.context
                val intent = Book.idTo(Intent(context, PlayAudiobookActivity::class.java), itemId)
                context.startActivity(intent)
            }

            titleView = view.findViewById(R.id.story_title)
            sizeView = view.findViewById(R.id.story_size)
            ageView = view.findViewById(R.id.story_age)
            lastOpenedView = view.findViewById(R.id.story_last_opened)
            openedCountView = view.findViewById(R.id.story_opened_count)
            starView = view.findViewById(R.id.story_star)

            starView.setOnClickListener { v: View ->
                val context = v.context

                val book = Book.find(context, itemId)
                val starred = !book.starred

                book.starred = starred
                book.save(context)

                v.isActivated = starred
            }
        }
    }

    private val DATE_FORMAT = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, cursor: Cursor) {
        val book = Book(cursor)

        holder.titleView.text = book.title

        holder.sizeView.text = getReadableTimeDuration(book.size)

        if (holder.ageView != null) {
            val age = DATE_FORMAT.format(Date(book.mtime))
            holder.ageView!!.text = age
        }

        val lastOpenedMillis = book.lastOpenedAt
        if (lastOpenedMillis > 0L) {
            val lastOpened = DATE_FORMAT.format(Date(lastOpenedMillis))
            holder.lastOpenedView.text = lastOpened
        }
        else {
            holder.lastOpenedView.text = "Never"
        }

        if (holder.openedCountView != null) {
            holder.openedCountView!!.text = book.openedCount.toString()
        }

        holder.starView.isActivated = book.starred
    }

    companion object {
        //Based on https://stackoverflow.com/a/63327131/14819
        fun getReadableTimeDuration(timeMs: Long): String {
            val formatter = Formatter()
            val totalSeconds = timeMs / 1000
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60 % 60
            val hours = totalSeconds / 3600

            return if (hours > 0) {
                formatter.format("%d:%d:%02d", hours, minutes, seconds).toString()
            }
            else {
                formatter.format("%d:%02d", minutes, seconds).toString()
            }
        }
    }
}