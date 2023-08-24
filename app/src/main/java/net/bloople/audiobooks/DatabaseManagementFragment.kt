package net.bloople.audiobooks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.IOException

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class DatabaseManagementFragment : Fragment() {
    private lateinit var importDatabaseButton: ImageButton
    private lateinit var exportDatabaseButton: ImageButton

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.database_management_fragment, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exportDatabaseButton = view.findViewById(R.id.export_database)
        exportDatabaseButton.setOnClickListener { startExport() }
        importDatabaseButton = view.findViewById(R.id.import_database)
        importDatabaseButton.setOnClickListener { startImport() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE_EXPORT && resultCode == Activity.RESULT_OK) completeExport(data!!)
        else if(requestCode == REQUEST_CODE_IMPORT && resultCode == Activity.RESULT_OK) completeImport(data!!)
    }

    private fun startExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/vnd.sqlite3"
        intent.putExtra(Intent.EXTRA_TITLE, "Audiobooks.db")
        startActivityForResult(intent, REQUEST_CODE_EXPORT)
    }

    private fun completeExport(data: Intent) {
        try {
            requireContext().contentResolver.openOutputStream(data.data!!).use {
                DatabaseHelper.exportDatabase(requireContext(), it!!)
            }

            Toast.makeText(context, "Database exported successfully", Toast.LENGTH_LONG).show()
        }
        catch(e: IOException) {
            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
        }
    }

    private fun startImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_CODE_IMPORT)
    }

    private fun completeImport(data: Intent) {
        try {
            requireContext().contentResolver.openInputStream(data.data!!).use {
                DatabaseHelper.importDatabase(requireContext(), it!!)
            }

            Toast.makeText(context, "Database imported successfully", Toast.LENGTH_LONG).show()
        }
        catch(e: IOException) {
            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_EXPORT = 0
        private const val REQUEST_CODE_IMPORT = 2
    }
}