package net.bloople.audiobooks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

class BooksAdapter extends CursorRecyclerAdapter<BooksAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView sizeView;
        TextView ageView;
        TextView lastOpenedView;
        TextView openedCountView;
        ImageButton starView;
        ViewHolder(View view) {
            super(view);

            view.setOnClickListener(v -> {
                Context context = v.getContext();

                Intent intent = Book.idTo(new Intent(context, PlayAudiobookActivity.class), getItemId());
                context.startActivity(intent);
            });

            titleView = view.findViewById(R.id.story_title);
            sizeView = view.findViewById(R.id.story_size);
            ageView = view.findViewById(R.id.story_age);
            lastOpenedView = view.findViewById(R.id.story_last_opened);
            openedCountView = view.findViewById(R.id.story_opened_count);
            starView = view.findViewById(R.id.story_star);

            starView.setOnClickListener(view1 -> {
                Context context = view1.getContext();
                long id = getItemId();

                Book book = Book.find(context, id);
                boolean starred = !book.getStarred();

                book.setStarred(starred);
                book.save(context);

                view1.setActivated(starred);
            });
        }
    }

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy",
            Locale.getDefault());

    BooksAdapter(Cursor cursor) {
        super(cursor);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BooksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent,
                false);

        return new BooksAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(BooksAdapter.ViewHolder holder, Cursor cursor) {
        Book book = new Book(cursor);

        holder.titleView.setText(book.getTitle());

        holder.sizeView.setText(getReadableTimeDuration(book.getSize()));

        if(holder.ageView != null) {
            String age = DATE_FORMAT.format(new Date(book.getMtime()));
            holder.ageView.setText(age);
        }

        long lastOpenedMillis = book.getLastOpenedAt();
        if(lastOpenedMillis > 0L) {
            String lastOpened = DATE_FORMAT.format(new Date(lastOpenedMillis));
            holder.lastOpenedView.setText(lastOpened);
        }
        else {
            holder.lastOpenedView.setText("Never");
        }

        if(holder.openedCountView != null) {
            holder.openedCountView.setText(String.valueOf(book.getOpenedCount()));
        }

        holder.starView.setActivated(book.getStarred());
    }

    //Based on https://stackoverflow.com/a/63327131/14819
    static String getReadableTimeDuration(long timeMs) {
        Formatter formatter = new Formatter();
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        if (hours > 0) {
            return formatter.format("%d:%d:%02d", hours, minutes, seconds).toString();
        }
        else {
            return formatter.format("%d:%02d", minutes, seconds).toString();
        }
    }
}
