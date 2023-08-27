@file:Suppress("DEPRECATION")

package net.bloople.audiobooks

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import java.io.File
import java.util.*
import java.util.regex.Pattern

@Suppress("OVERRIDE_DEPRECATION")
internal class IndexingTask(private val context: Context, private val indexable: Indexable) :
    AsyncTask<String, Int, Unit>() {
    private var progress = 0
    private var max = 0
    private val metrics = IndexingMetrics()

    override fun doInBackground(vararg params: String) {
        destroyDeleted()
        indexDirectory(File(params[0]))
        publishProgress(progress, max)
    }

    override fun onProgressUpdate(vararg args: Int?) {
        indexable.onIndexingProgress(args[0]!!, args[1]!!)
    }

    override fun onPostExecute(result: Unit?) {
        indexable.onIndexingComplete(metrics)
    }

    private fun destroyDeleted() {
        val db = DatabaseHelper.instance(context)
        db.query("books", null, null, null, null, null, null).use {
            max += it.count
            while (it.moveToNext()) {
                val book = Book(it)
                val file = File(book.path!!)
                if (!file.exists()) {
                    book.destroy(context)
                    metrics.deleted++
                }

                progress++
                publishProgress(progress, max)
            }
        }
    }

    private fun indexDirectory(directory: File) {
        val files = directory.listFiles() ?: return
        val filesToIndex = ArrayList<File>()

        max += filesToIndex.size
        publishProgress(progress, max)

        for (f in files) {
            if (f.isDirectory) indexDirectory(f)
            else if (AUDIOBOOK_EXTENSIONS.matcher(f.name).find()) filesToIndex.add(f)
        }

        for (f in filesToIndex) indexFile(f)
    }

    private fun indexFile(file: File) {
        val book = Book.findByPathOrNull(context, file.canonicalPath) ?: Book()
        val isNew = book.isNew

        if(file.lastModified() == book.mtime) {
            metrics.skipped++
        }
        else {
            book.edit(context) {
                val filePath = file.canonicalPath
                path = filePath
                title = AUDIOBOOK_EXTENSIONS.matcher(file.name).replaceFirst("")
                mtime = file.lastModified()
                size = getDuration(filePath).toLong()
            }
            if(isNew) metrics.created++ else metrics.updated++
        }

        progress++
        publishProgress(progress, max)
    }

    private fun getDuration(path: String): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration?.toInt() ?: 0
        } catch (e: RuntimeException) {
            e.printStackTrace()
            0
        }
    }

    companion object {
        val AUDIOBOOK_EXTENSIONS: Pattern = Pattern.compile("(?i)\\.(m4a|mkv|mp3|mp4|wav|webm)$")
    }
}