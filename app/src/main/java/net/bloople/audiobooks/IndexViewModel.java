package net.bloople.audiobooks;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndexViewModel extends AndroidViewModel {
    private Application application;
    private MutableLiveData<Cursor> searchResults;
    private BooksSearcher searcher = new BooksSearcher();
    private MutableLiveData<String> sorterDescription;

    public IndexViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<Cursor> getSearchResults() {
        if(searchResults == null) {
            searchResults = new MutableLiveData<>();
            resolve();
        }
        return searchResults;
    }

    public LiveData<String> getSorterDescription() {
        if(sorterDescription == null) {
            sorterDescription = new MutableLiveData<>(searcher.description());
        }
        return sorterDescription;
    }

    public int getSortMethod() {
        return searcher.getSortMethod();
    }

    public boolean getSortDirectionAsc() {
        return searcher.getSortDirectionAsc();
    }

    public void setSearchText(String searchText) {
        searcher.setSearchText(searchText);
        resolve();
    }

    public void setSort(int sortMethod, boolean sortDirectionAsc) {
        searcher.setSortMethod(sortMethod);
        searcher.setSortDirectionAsc(sortDirectionAsc);
        sorterDescription.setValue(searcher.description());
        resolve();
    }

    private void resolve() {
        ExecutorService service =  Executors.newSingleThreadExecutor();
        service.submit(() -> {
            searchResults.postValue(searcher.search(application));
        });
    }
}
