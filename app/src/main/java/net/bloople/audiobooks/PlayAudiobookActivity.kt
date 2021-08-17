package net.bloople.audiobooks

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import com.google.android.exoplayer2.ui.StyledPlayerControlView

class PlayAudiobookActivity : Activity() {
    private var bookId: Long = -1
    private var playerView: StyledPlayerControlView? = null

    /**
     * Create our connection to the service to be used in our bindService call.
     */
    private var serviceBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            playerView?.let { playerView!!.player = null }
        }

        /**
         * Called after a successful bind with our PlayerService.
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceBound = true
            if (service is PlayerService.PlayerServiceBinder) {
                val holder = service.getPlayerHolderInstance() // use the player and call methods on it to start and stop
                playerView?.let { playerView!!.player = holder.audioFocusPlayer }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_audiobook)

        val intent: Intent = getIntent()
        bookId = intent.getLongExtra("_id", -1)

        val book = Book.findById(this, bookId)
        book.lastOpenedAt(System.currentTimeMillis())
        book.save(this)

        val titleView: TextView = findViewById(R.id.exo_title)
        titleView.setText(book.title())

        playerView = findViewById(R.id.player)

        val playerIntent = Intent(this.applicationContext, PlayerService::class.java).apply {
            putExtra("_id", book.id())
        }
        startService(playerIntent)
        bindService(playerIntent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        val book = Book.findById(this, bookId)
        book.lastOpenedAt(System.currentTimeMillis())
        book.save(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(serviceBound) {
            serviceBound = false
            unbindService(connection)
        }
    }
}