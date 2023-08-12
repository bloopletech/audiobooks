package net.bloople.audiobooks

import android.Manifest
import net.bloople.audiobooks.DatabaseHelper.deleteDatabase
import android.app.Activity
import net.bloople.audiobooks.Indexable
import android.os.Bundle
import net.bloople.audiobooks.R
import android.content.pm.PackageManager
import net.bloople.audiobooks.IndexingActivity
import net.bloople.audiobooks.IndexingTask
import net.bloople.audiobooks.DatabaseHelper
import android.widget.TextView.OnEditorActionListener
import android.view.inputmethod.EditorInfo
import android.content.Intent
import android.content.SharedPreferences
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

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

        val permission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission == PackageManager.PERMISSION_GRANTED) canAccessFiles = true
        else requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)

        progressBar = findViewById(R.id.indexing_progress)
        indexButton = findViewById(R.id.index_button)
        indexButton.setOnClickListener {
            indexButton.setEnabled(false)
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_EXTERNAL_STORAGE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) canAccessFiles = true
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
        // Storage Permissions
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}