package net.bloople.audiobooks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BooksSearcher {
    public static final int SORT_ALPHABETIC = 0;
    public static final int SORT_AGE = 1;
    public static final int SORT_SIZE = 2;
    public static final int SORT_LAST_OPENED = 3;
    public static final int SORT_STARRED = 4;
    public static final int SORT_OPENED_COUNT = 5;

    private String searchText = "";
    private int sortMethod = SORT_AGE;
    private boolean sortDirectionAsc = false;

    BooksSearcher() {
    }

    void setSearchText(String inSearchText) {
        searchText = inSearchText;
    }

    int getSortMethod() {
        return sortMethod;
    }

    void setSortMethod(int sortMethod) {
        this.sortMethod = sortMethod;
    }

    boolean getSortDirectionAsc() {
        return sortDirectionAsc;
    }

    void setSortDirectionAsc(boolean sortDirectionAsc) {
        this.sortDirectionAsc = sortDirectionAsc;
    }

    void flipSortDirection() {
        sortDirectionAsc = !sortDirectionAsc;
    }

    String description() {
        return "Sorted by " + sortMethodDescription().toLowerCase() + " " + sortDirectionDescription().toLowerCase();
    }

    String sortMethodDescription() {
        switch(sortMethod) {
            case SORT_ALPHABETIC: return "Title";
            case SORT_AGE: return "Published Date";
            case SORT_SIZE: return "Size";
            case SORT_LAST_OPENED: return "Last Opened At";
            case SORT_OPENED_COUNT: return "Opened Count";
            //case SORT_RANDOM: return "Random";
            case SORT_STARRED: return "Starred";
            default: throw new IllegalStateException("sort_method not in valid range");
        }
    }

    String sortDirectionDescription() {
        return sortDirectionAsc ? "Ascending" : "Descending";
    }

    private String orderBy() {
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
            case SORT_OPENED_COUNT:
                orderBy += "opened_count";
                break;
        }

        if(sortDirectionAsc) orderBy += " ASC";
        else orderBy += " DESC";

        orderBy += ", title ASC";

        return orderBy;
    }

    public Cursor search(Context context) {
        SQLiteDatabase db = DatabaseHelper.instance(context);

        Cursor cursor;
        if(!searchText.equals("")) {
            cursor = db.query("books", null, "title LIKE ?",
                new String[] { "%" + searchText + "%" }, null, null, orderBy());
        }
        else {
            cursor = db.query("books", null, null, null, null, null, orderBy());
        }

        cursor.moveToFirst();

        return cursor;
    }
}
