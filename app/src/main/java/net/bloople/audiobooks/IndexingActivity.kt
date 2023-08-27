package net.bloople.audiobooks

import net.bloople.audiobooks.DatabaseHelper.deleteDatabase
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.view.KeyEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class IndexingActivity : AppCompatActivity(), Indexable {
    private lateinit var progressBar: ProgressBar
    private lateinit var indexButton: Button
    private lateinit var indexRoot: String
    private var canAccessFiles = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_indexing)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if(Environment.isExternalStorageManager()) {
            canAccessFiles = true
        }
        else {
            val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri),
                REQUEST_CODE_MANAGE_EXTERNAL_STORAGE
            )
        }

        progressBar = findViewById(R.id.indexing_progress)
        indexButton = findViewById(R.id.index_button)
        indexButton.setOnClickListener {
            indexButton.isEnabled = false

            if (canAccessFiles) {
                val indexer = IndexingTask(this@IndexingActivity,this@IndexingActivity)
                indexer.execute(indexRoot)
            }
        }

        val deleteIndexButton = findViewById<Button>(R.id.delete_index_button)
        deleteIndexButton.setOnClickListener {
            deleteDatabase(this@IndexingActivity)
            Toast.makeText(this@IndexingActivity, "Index deleted.", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
        }

        loadPreferences()

        val indexDirectoryText: EditText = findViewById(R.id.index_directory)
        indexDirectoryText.setText(indexRoot)
        indexDirectoryText.setOnEditorActionListener { _, actionId, event ->
            if(event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                indexRoot = indexDirectoryText.text.toString()
                savePreferences()
            }
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_MANAGE_EXTERNAL_STORAGE) canAccessFiles = Environment.isExternalStorageManager()
    }

    private fun preferences(): SharedPreferences {
        return applicationContext.getSharedPreferences("main", MODE_PRIVATE)
    }

    private fun loadPreferences() {
        val preferences = preferences()
        indexRoot = preferences.getString("index-root", Environment.getExternalStorageDirectory().absolutePath).toString()
    }

    private fun savePreferences() {
        val editor = preferences().edit()
        editor.putString("index-root", indexRoot)
        editor.apply()
    }

    override fun onIndexingProgress(progress: Int, max: Int) {
        progressBar.progress = progress
        progressBar.max = max
    }

    override fun onIndexingComplete(count: Int) {
        setResult(RESULT_OK)
        indexButton.isEnabled = true
        Toast.makeText(
            this@IndexingActivity, "Indexing complete, $count audiobooks indexed.",
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        private const val REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 1
    }
}