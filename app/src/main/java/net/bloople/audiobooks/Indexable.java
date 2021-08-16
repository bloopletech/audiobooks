package net.bloople.audiobooks;

 interface Indexable {
    void onIndexingProgress(int progress, int max);
    void onIndexingComplete(int count);
}
