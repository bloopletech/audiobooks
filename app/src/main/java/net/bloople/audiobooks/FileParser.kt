package net.bloople.audiobooks

import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.Closeable
import java.io.File

data class ParsedFile(val path: String, val title: String, val mtime: Long, val size: Long)

class FileParser: Closeable {
    private val retriever = MediaMetadataRetriever()

    override fun close() {
        retriever.close()
    }

    fun parse(file: File): ParsedFile {
        return ParsedFile(
            file.canonicalPath,
            file.nameWithoutExtension,
            file.lastModified(),
            getDuration(file.canonicalPath).toLong()
        )
    }

    private fun getDuration(path: String): Int {
        return try {
            retriever.setDataSource(path)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration?.toInt() ?: 0
        }
        catch (e: RuntimeException) {
            Log.e(TAG, "Error while trying to extract duration for $path", e)
            0
        }
    }

    companion object {
        private const val TAG = "FileParser"
    }
}

