package net.bloople.audiobooks

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import java.io.File
import java.util.*
import java.util.regex.Pattern

internal class IndexingTask(private val context: Context, private val indexable: Indexable) :
    AsyncTask<String?, Int?, Void?>() {
    private var progress = 0
    private var max = 0
    private var indexed = 0
    override fun doInBackground(vararg params: String?): Void? {
        destroyDeleted()
        indexDirectory(File(params[0]))
        publishProgress(progress, max)
        return null
    }

    override fun onProgressUpdate(vararg args: Int?) {
        indexable.onIndexingProgress(args[0]!!, args[1]!!)
    }

    override fun onPostExecute(result: Void?) {
        indexable.onIndexingComplete(indexed)
    }

    private fun destroyDeleted() {
        val db = DatabaseHelper.instance(context)
        db.query("books", null, null, null, null, null, null).use {
            max += it.count
            while (it.moveToNext()) {
                val book = Book(it)
                val file = File(book.path)
                if (!file.exists()) book.destroy(context)
                progress++
                publishProgress(progress, max)
            }
        }
    }

    private fun indexDirectory(directory: File) {
        val files = directory.listFiles() ?: return
        val filesToIndex = ArrayList<File>()
        for (f in files) {
            if (f.isDirectory) {
                indexDirectory(f)
            } else {
                if (AUDIOBOOK_EXTENSIONS.matcher(f.name).find()) filesToIndex.add(f)
            }
        }
        max += filesToIndex.size
        publishProgress(progress, max)
        for (f in filesToIndex) indexFile(f)
    }

    private fun indexFile(file: File) {
        (Book.findByPathOrNull(context, file.canonicalPath) ?: Book()).edit(context) {
            val filePath = file.canonicalPath
            path = filePath
            title = AUDIOBOOK_EXTENSIONS.matcher(file.name).replaceFirst("")
            mtime = file.lastModified()
            size = getDuration(filePath).toLong()
        }
        progress++
        indexed++

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
        val AUDIOBOOK_EXTENSIONS = Pattern.compile("\\.(mp3|m4a)$")
    }
}