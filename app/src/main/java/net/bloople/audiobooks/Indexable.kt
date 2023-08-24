package net.bloople.audiobooks

interface Indexable {
    fun onIndexingProgress(progress: Int, max: Int)
    fun onIndexingComplete(count: Int)
}