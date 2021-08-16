package net.bloople.audiobooks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;

import java.io.File;

public class PlayAudiobookActivity extends Activity {
    private Book book;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_audiobook);

        Intent intent = getIntent();
        book = Book.findById(this, intent.getLongExtra("_id", -1));

        book.lastOpenedAt(System.currentTimeMillis());
        book.save(this);

        TextView bookPathView = findViewById(R.id.book_path);
        bookPathView.setText(book.path());

        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)
            .setMp3ExtractorFlags(Mp3Extractor.FLAG_ENABLE_INDEX_SEEKING);

        File myFile = new File(book.path());
        String bookUrl = String.valueOf(Uri.fromFile(myFile));

        MediaItem mediaItem = MediaItem.fromUri(bookUrl);

        player = new SimpleExoPlayer.Builder(this)
            .setMediaSourceFactory(
                new DefaultMediaSourceFactory(this, extractorsFactory))
            .build();

        StyledPlayerControlView playerView = findViewById(R.id.player);
        playerView.setPlayer(player);

        player.setMediaItem(mediaItem);
        player.seekTo(book.lastReadPosition());
        player.setPlayWhenReady(true);

        player.prepare();
    }

    @Override
    protected void onStop() {
        super.onStop();

        book.lastOpenedAt(System.currentTimeMillis());
        book.save(this);

        savePosition();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    public void savePosition() {
        long currentReadPosition = player.getCurrentPosition();
        if(player.getPlaybackState() == Player.STATE_ENDED) currentReadPosition = 0;

        book.lastReadPosition(currentReadPosition);
        book.save(this);
    }
}
