package net.bloople.audiobooks;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class IndexingActivity extends Activity implements Indexable {
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ProgressBar progressBar;
    private Button indexButton;
    private String indexRoot;
    private boolean canAccessFiles = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indexing);

        int permission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission == PackageManager.PERMISSION_GRANTED) canAccessFiles = true;
        else requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);

        progressBar = findViewById(R.id.indexing_progress);

        indexButton = findViewById(R.id.index_button);
        indexButton.setOnClickListener(v -> {
            indexButton.setEnabled(false);

            if(canAccessFiles) {
                IndexingTask indexer = new IndexingTask(IndexingActivity.this,
                        IndexingActivity.this);
                indexer.execute(indexRoot);
            }
        });

        Button deleteIndexButton = findViewById(R.id.delete_index_button);
        deleteIndexButton.setOnClickListener(v -> {
            DatabaseHelper.deleteDatabase(IndexingActivity.this);
            Toast.makeText(IndexingActivity.this, "Index deleted.", Toast.LENGTH_SHORT).show();
        });

        loadPreferences();

        final EditText indexDirectoryText = findViewById(R.id.index_directory);
        indexDirectoryText.setText(indexRoot);

        indexDirectoryText.setOnEditorActionListener((v, actionId, event) -> {
            if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                    (actionId == EditorInfo.IME_ACTION_DONE)) {
                indexRoot = indexDirectoryText.getText().toString();
                savePreferences();
            }
            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_CANCELED) finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if(requestCode == REQUEST_EXTERNAL_STORAGE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) canAccessFiles = true;
    }

    private SharedPreferences preferences() {
        return getApplicationContext().getSharedPreferences("main", Context.MODE_PRIVATE);
    }

    private void loadPreferences() {
        SharedPreferences preferences = preferences();

        indexRoot = preferences.getString("index-root",
                Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = preferences().edit();
        editor.putString("index-root", indexRoot);
        editor.apply();
    }

    public void onIndexingProgress(int progress, int max) {
        progressBar.setProgress(progress);
        progressBar.setMax(max);
    }

    public void onIndexingComplete(int count) {
        setResult(RESULT_OK);
        indexButton.setEnabled(true);
        Toast.makeText(IndexingActivity.this, "Indexing complete, " + count + " audiobooks indexed.",
                Toast.LENGTH_LONG).show();
    }
}
