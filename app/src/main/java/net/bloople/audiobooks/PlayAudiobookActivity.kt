package net.bloople.audiobooks

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import com.beraldo.playerlib.PlayerService
import com.beraldo.playerlib.PlayerService.Companion.STREAM_POSITION
import com.beraldo.playerlib.PlayerService.Companion.STREAM_URL
import java.io.File

class PlayAudiobookActivity : Activity() {
    private var book: Book? = null

    /**
     * Create our connection to the service to be used in our bindService call.
     */
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {}

        /**
         * Called after a successful bind with our PlayerService.
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.PlayerServiceBinder) {
                service.getPlayerHolderInstance() // use the player and call methods on it to start and stop
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_audiobook)

        val intent: Intent = getIntent()
        book = Book.findById(this, intent.getLongExtra("_id", -1))
        book!!.lastOpenedAt(System.currentTimeMillis())
        book!!.save(this)

        val bookPathView: TextView = findViewById<TextView>(R.id.book_path)
        bookPathView.setText(book!!.path())

//        val extractorsFactory: DefaultExtractorsFactory = DefaultExtractorsFactory()
//            .setConstantBitrateSeekingEnabled(true)
//            .setMp3ExtractorFlags(Mp3Extractor.FLAG_ENABLE_INDEX_SEEKING)
        val myFile = File(book!!.path())
        val bookUrl = Uri.fromFile(myFile).toString()
//        val mediaItem: MediaItem = MediaItem.fromUri(bookUrl)
//        player = Builder(this)
//            .setMediaSourceFactory(
//                DefaultMediaSourceFactory(this, extractorsFactory)
//            )
//            .build()
//        player.setWakeMode(C.WAKE_MODE_LOCAL)
//        val playerView: StyledPlayerControlView = findViewById(R.id.player)
//        playerView.setPlayer(player)
//        player.setMediaItem(mediaItem)
//        player.seekTo(book.lastReadPosition())
//        player.setPlayWhenReady(true)
//        player.prepare()
        //Start the service
        val playerIntent = Intent(this, PlayerService::class.java).apply {
            putExtra(STREAM_URL, bookUrl)
            putExtra(STREAM_POSITION, book!!.lastReadPosition())
        }
        bindService(playerIntent, connection, Context.BIND_AUTO_CREATE)
    }
//
//    protected override fun onStop() {
//        super.onStop()
//        book!!.lastOpenedAt(System.currentTimeMillis())
//        book!!.save(this)
//        savePosition()
//    }
//
//    override fun onBackPressed() {
//        super.onBackPressed()
//        finish()
//    }
//
//    protected override fun onDestroy() {
//        super.onDestroy()
//        player.release()
//    }
//
//    fun savePosition() {
//        var currentReadPosition: Long = player.getCurrentPosition()
//        if (player.getPlaybackState() === Player.STATE_ENDED) currentReadPosition = 0
//        book!!.lastReadPosition(currentReadPosition)
//        book!!.save(this)
//    }
}