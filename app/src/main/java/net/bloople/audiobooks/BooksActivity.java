package net.bloople.audiobooks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class BooksActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_INDEXING = 0;

    private IndexViewModel model;
    private RecyclerView listView;
    private BooksAdapter adapter;
    private LinearLayoutManager layoutManager;
    private TextView searchResultsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        model = new ViewModelProvider(this).get(IndexViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchResultsToolbar = findViewById(R.id.search_results_toolbar);

        model.getSorterDescription().observe(this, description -> {
            searchResultsToolbar.setText(description);
        });

        final EditText searchField = (EditText)findViewById(R.id.searchText);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
                    searchField.clearFocus();

                    model.setSearchText(v.getText().toString());

                    handled = true;
                }
                return handled;
            }
        });

        searchField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    int clickIndex = searchField.getRight() -
                        searchField.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();

                    if(event.getRawX() >= clickIndex) {
                        searchField.setText("");
                        searchField.clearFocus();

                        model.setSearchText("");

                        return true;
                    }
                }
                return false;
            }
        });

        listView = (RecyclerView)findViewById(R.id.audiobooks_list);
        adapter = new BooksAdapter(null);
        listView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        model.getSearchResults().observe(this, searchResults -> {
            adapter.swapCursor(searchResults);
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        model.setSort(savedInstanceState.getInt("sortMethod"), savedInstanceState.getBoolean("sortDirectionAsc"));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("sortMethod", model.getSortMethod());
        savedInstanceState.putBoolean("sortDirectionAsc", model.getSortDirectionAsc());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);

        MenuCompat.setGroupDividerEnabled(menu, true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int sortMethod = model.getSortMethod();
        int newSortMethod = sortMethod;

        if(menuItem.getItemId() == R.id.sort_alphabetic) {
            newSortMethod = BooksSearcher.SORT_ALPHABETIC;
        }
        else if(menuItem.getItemId() == R.id.sort_age) {
            newSortMethod = BooksSearcher.SORT_AGE;
        }
        else if(menuItem.getItemId() == R.id.sort_size) {
            newSortMethod = BooksSearcher.SORT_SIZE;
        }
        else if(menuItem.getItemId() == R.id.sort_last_opened) {
            newSortMethod = BooksSearcher.SORT_LAST_OPENED;
        }
        else if(menuItem.getItemId() == R.id.sort_starred) {
            newSortMethod = BooksSearcher.SORT_STARRED;
        }
        else if(menuItem.getItemId() == R.id.sort_opened_count) {
            newSortMethod = BooksSearcher.SORT_OPENED_COUNT;
        }
        else if(menuItem.getItemId() == R.id.manage_indexing) {
            Intent intent = new Intent(BooksActivity.this, IndexingActivity.class);
            startActivityForResult(intent, REQUEST_CODE_INDEXING);
            return true;
        }

        boolean sortDirectionAsc = model.getSortDirectionAsc();
        if(sortMethod == newSortMethod) sortDirectionAsc = !sortDirectionAsc;
        model.setSort(newSortMethod, sortDirectionAsc);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_INDEXING && resultCode == RESULT_OK) {
            model.refresh();
        }
    }
}
