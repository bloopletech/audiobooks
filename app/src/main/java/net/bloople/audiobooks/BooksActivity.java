package net.bloople.audiobooks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toolbar;

public class BooksActivity extends Activity {
    public static final int SORT_ALPHABETIC = 0;
    public static final int SORT_AGE = 1;
    public static final int SORT_SIZE = 2;
    public static final int SORT_LAST_OPENED = 3;
    public static final int SORT_STARRED = 4;

    private int sortMethod;
    private boolean sortDirectionAsc;
    private String searchText = "";

    private RecyclerView listView;
    private BooksAdapter adapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setActionBar(toolbar);

        final EditText searchField = (EditText)findViewById(R.id.searchText);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
                    searchField.clearFocus();

                    searchText = v.getText().toString();
                    updateCursor();

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
                        searchText = "";
                        updateCursor();

                        return true;
                    }
                }
                return false;
            }
        });

        listView = (RecyclerView)findViewById(R.id.stories_list);
        adapter = new BooksAdapter(null);
        listView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();

        loadPreferences();
        updateCursor();
    }

    @Override
    public void onStop() {
        super.onStop();

        savePreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.sort_alphabetic) {
            if(sortMethod == SORT_ALPHABETIC) sortDirectionAsc = !sortDirectionAsc;
            sortMethod = SORT_ALPHABETIC;
        }
        else if(menuItem.getItemId() == R.id.sort_age) {
            if(sortMethod == SORT_AGE) sortDirectionAsc = !sortDirectionAsc;
            sortMethod = SORT_AGE;
        }
        else if(menuItem.getItemId() == R.id.sort_size) {
            if(sortMethod == SORT_SIZE) sortDirectionAsc = !sortDirectionAsc;
            sortMethod = SORT_SIZE;
        }
        else if(menuItem.getItemId() == R.id.sort_last_opened) {
            if(sortMethod == SORT_LAST_OPENED) sortDirectionAsc = !sortDirectionAsc;
            sortMethod = SORT_LAST_OPENED;
        }
        else if(menuItem.getItemId() == R.id.sort_starred) {
            if(sortMethod == SORT_STARRED) sortDirectionAsc = !sortDirectionAsc;
            sortMethod = SORT_STARRED;
        }
        else if(menuItem.getItemId() == R.id.manage_indexing) {
            Intent intent = new Intent(BooksActivity.this, IndexingActivity.class);
            startActivity(intent);
        }

        updateCursor();

        return true;
    }

    private SharedPreferences preferences() {
        return getApplicationContext().getSharedPreferences("books-list", Context.MODE_PRIVATE);
    }

    private void loadPreferences() {
        SharedPreferences preferences = preferences();

        sortMethod = preferences.getInt("last-sort-method", SORT_LAST_OPENED);
        sortDirectionAsc = preferences.getBoolean("last-sort-direction-asc", false);
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = preferences().edit();
        editor.putInt("last-sort-method", sortMethod);
        editor.putBoolean("last-sort-direction-asc", sortDirectionAsc);
        editor.apply();
    }

    private void updateCursor() {
        String orderBy = "";

        switch(sortMethod) {
            case SORT_ALPHABETIC:
                orderBy += "title";
                break;
            case SORT_AGE:
                orderBy += "mtime";
                break;
            case SORT_SIZE:
                orderBy += "size";
                break;
            case SORT_LAST_OPENED:
                orderBy += "last_opened_at";
                break;
            case SORT_STARRED:
                orderBy += "starred";
                break;
        }

        if(sortDirectionAsc) orderBy += " ASC";
        else orderBy += " DESC";

        orderBy += ", title ASC";

        SearchBooksTask searcher = new SearchBooksTask();
        searcher.execute(searchText, orderBy);
    }

    private class SearchBooksTask extends AsyncTask<String, Void, Cursor> {
        protected Cursor doInBackground(String... params) {
            String searchText = params[0];
            String orderBy = params[1];

            SQLiteDatabase db = DatabaseHelper.instance(BooksActivity.this);

            Cursor cursor;
            if(!searchText.equals("")) {
                cursor = db.query("books", null, "title LIKE ?",
                        new String[] { "%" + searchText + "%" }, null, null, orderBy);
            }
            else {
                cursor = db.query("books", null, null, null, null, null, orderBy);
            }

            cursor.moveToFirst();

            return cursor;
        }

        protected void onPostExecute(Cursor cursor) {
            adapter.swapCursor(cursor);
        }
    }
}
