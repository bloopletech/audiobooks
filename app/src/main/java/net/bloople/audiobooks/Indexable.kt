package net.bloople.audiobooks

interface Indexable {
    fun onIndexingProgress(progress: Int, max: Int)
    fun onIndexingComplete(metrics: IndexingMetrics)
}