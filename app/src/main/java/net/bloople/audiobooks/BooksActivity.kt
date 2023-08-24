package net.bloople.audiobooks

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class BooksActivity : AppCompatActivity() {
    private lateinit var model: IndexViewModel
    private lateinit var listView: RecyclerView
    private lateinit var adapter: BooksAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var searchResultsToolbar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)

        model = ViewModelProvider(this)[IndexViewModel::class.java]

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        searchResultsToolbar = findViewById(R.id.search_results_toolbar)

        model.sorterDescription.observe(this) { description: String? -> searchResultsToolbar.text = description }

        val searchField = findViewById<View>(R.id.searchText) as EditText
        searchField.setOnEditorActionListener { v, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchField.windowToken, 0)
                searchField.clearFocus()

                model.setSearchText(v.text.toString())

                handled = true
            }
            handled
        }

        searchField.setOnTouchListener(OnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                val clickIndex = searchField.right - searchField.compoundDrawables[DRAWABLE_RIGHT].bounds.width()

                if (event.rawX >= clickIndex) {
                    searchField.setText("")
                    searchField.clearFocus()

                    model.setSearchText("")

                    return@OnTouchListener true
                }
            }
            false
        })

        listView = findViewById(R.id.audiobooks_list)
        adapter = BooksAdapter(null)
        listView.adapter = adapter

        layoutManager = LinearLayoutManager(this)
        listView.layoutManager = layoutManager

        model.searchResults.observe(this) { searchResults: Cursor? -> adapter.swapCursor(searchResults) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        model.setSort(savedInstanceState.getInt("sortMethod"), savedInstanceState.getBoolean("sortDirectionAsc"))
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt("sortMethod", model.sortMethod)
        savedInstanceState.putBoolean("sortDirectionAsc", model.sortDirectionAsc)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        val inflater = menuInflater
        inflater.inflate(R.menu.list_menu, menu)

        MenuCompat.setGroupDividerEnabled(menu, true)

        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val sortMethod = model.sortMethod
        var newSortMethod = sortMethod

        when (menuItem.itemId) {
            R.id.sort_alphabetic -> newSortMethod = BooksSearcher.SORT_ALPHABETIC
            R.id.sort_age -> newSortMethod = BooksSearcher.SORT_AGE
            R.id.sort_size -> newSortMethod = BooksSearcher.SORT_SIZE
            R.id.sort_last_opened -> newSortMethod = BooksSearcher.SORT_LAST_OPENED
            R.id.sort_starred -> newSortMethod = BooksSearcher.SORT_STARRED
            R.id.sort_opened_count -> newSortMethod = BooksSearcher.SORT_OPENED_COUNT
            R.id.manage_indexing -> {
                val intent = Intent(this@BooksActivity, IndexingActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_INDEXING)
                return true
            }
        }

        var sortDirectionAsc = model.sortDirectionAsc
        if (sortMethod == newSortMethod) sortDirectionAsc = !sortDirectionAsc
        model.setSort(newSortMethod, sortDirectionAsc)

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_INDEXING && resultCode == RESULT_OK) {
            model.refresh()
        }
    }

    companion object {
        private const val REQUEST_CODE_INDEXING = 0
    }
}