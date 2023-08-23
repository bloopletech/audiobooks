package net.bloople.audiobooks

internal interface Indexable {
    fun onIndexingProgress(progress: Int, max: Int)
    fun onIndexingComplete(count: Int)
}