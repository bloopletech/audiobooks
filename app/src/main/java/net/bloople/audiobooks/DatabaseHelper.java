package net.bloople.audiobooks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class DatabaseHelper {
    private static final String DB_NAME = "books";
    private static SQLiteDatabase database;

    private static SQLiteDatabase obtainDatabase(Context context) {
        SQLiteDatabase db = context.getApplicationContext().openOrCreateDatabase(DB_NAME,
                Context.MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS books ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "path TEXT, " +
                "title TEXT, " +
                "mtime INT DEFAULT 0, " +
                "size INTEGER DEFAULT 0, " +
                "last_opened_at INTEGER DEFAULT 0, " +
                "last_read_position INTEGER DEFAULT 0, " +
                "starred INTEGER DEFAULT 0" +
                ")");

        return db;
    }

    static SQLiteDatabase instance(Context context) {
        if(database == null) {
            database = obtainDatabase(context);
        }

        return database;
    }

    static void deleteDatabase(Context context) {
        context.getApplicationContext().deleteDatabase(DB_NAME);
        database = null;
    }
}
