package net.bloople.audiobooks

import android.app.Application
import android.database.Cursor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.Executors

class IndexViewModel(application: Application) : AndroidViewModel(application) {
    private val searcher = BooksSearcher()

    val searchResults: MutableLiveData<Cursor> by lazy {
        MutableLiveData<Cursor>().also { resolve() }
    }

    val sorterDescription: MutableLiveData<String> by lazy {
        MutableLiveData<String>(searcher.description())
    }

    val sortMethod: Int
        get() = searcher.sortMethod
    val sortDirectionAsc: Boolean
        get() = searcher.sortDirectionAsc

    fun setSearchText(searchText: String) {
        searcher.setSearchText(searchText)
        resolve()
    }

    fun setSort(sortMethod: Int, sortDirectionAsc: Boolean) {
        searcher.sortMethod = sortMethod
        searcher.sortDirectionAsc = sortDirectionAsc
        sorterDescription.value = searcher.description()
        resolve()
    }

    fun refresh() {
        resolve()
    }

    private fun resolve() {
        val service = Executors.newSingleThreadExecutor()
        service.submit { searchResults.postValue(searcher.search(getApplication())) }
    }
}