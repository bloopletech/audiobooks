@file:Suppress("DEPRECATION")

package net.bloople.audiobooks

import android.content.Context
import android.os.AsyncTask
import java.io.File
import java.util.*

@Suppress("OVERRIDE_DEPRECATION")
internal class IndexingTask(private val context: Context, private val indexable: Indexable) :
    AsyncTask<String, Int, Unit>() {
    private var progress = 0
    private var max = 0
    private val metrics = IndexingMetrics()
    private val fileParser = FileParser()

    override fun doInBackground(vararg params: String) {
        destroyDeleted()
        fileParser.use { indexDirectory(File(params[0])) }
        publishProgress(progress, max)
    }

    override fun onProgressUpdate(vararg args: Int?) {
        indexable.onIndexingProgress(args[0]!!, args[1]!!)
    }

    override fun onPostExecute(result: Unit?) {
        indexable.onIndexingComplete(metrics)
    }

    private fun destroyDeleted() {
        Book.findAll(context) {
            max += it.count
            while (it.moveToNext()) {
                val book = Book(it)
                if (!book.file.exists()) {
                    book.destroy(context)
                    metrics.deleted++
                }

                progress++
                publishProgress(progress, max)
            }
        }
    }

    private fun indexDirectory(directory: File) {
        val (directories, files) = directory.listFiles()?.partition { it.isDirectory } ?: return
        for(d in directories) { indexDirectory(d) }

        files.filter { AUDIOBOOK_EXTENSIONS.contains(it.extension.lowercase()) }.also {
            max += it.size
            publishProgress(progress, max)
        }.forEach(::indexFile)
    }

    private fun indexFile(file: File) {
        val parsedFile by lazy { fileParser.parse(file) }
        val book = Book.findByPathOrNull(context, file.canonicalPath)
        if(book != null) updateBook(parsedFile, book)
        else createBook(parsedFile)

        progress++
        publishProgress(progress, max)
    }

    private fun createBook(parsedFile: ParsedFile) {
        NewBook(parsedFile.path, parsedFile.title, parsedFile.mtime, parsedFile.size).save(context)
        metrics.created++
    }

    private fun updateBook(parsedFile: ParsedFile, book: Book) {
        if(parsedFile.mtime == book.mtime) {
            metrics.skipped++
            return
        }

        book.edit(context) {
            title = parsedFile.title
            mtime = parsedFile.mtime
            size = parsedFile.size
        }
        metrics.updated++
    }

    companion object {
        val AUDIOBOOK_EXTENSIONS = listOf("m4a", "mkv", "mp3", "mp4", "wav", "webm")
    }
}