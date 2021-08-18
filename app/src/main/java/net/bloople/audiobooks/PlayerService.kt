/*
 * Copyright 2018 Filippo Beraldo. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//Changed by Brenton Fletcher

package net.bloople.audiobooks

import android.app.IntentService
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import androidx.media.AudioAttributesCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import net.bloople.audiobooks.media.DescriptionAdapter
import net.bloople.audiobooks.player.PlayerState
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import net.bloople.audiobooks.player.AudioFocusWrapper

/**
 * Created by Filippo Beraldo on 15/11/2018.
 * http://github.com/beraldofilippo
 */
class PlayerService : IntentService("audiobooks") {
    companion object {
        const val NOTIFICATION_ID = 100
        const val NOTIFICATION_CHANNEL = "audiobooks_channel"
    }

    var bookId: Long = -1

    private lateinit var player: ExoPlayer
    private var playerState: PlayerState? = null

    private lateinit var playerNotificationManager: PlayerNotificationManager

    override fun onCreate() {
        super.onCreate()

        val extractorsFactory: DefaultExtractorsFactory = DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)
            .setMp3ExtractorFlags(Mp3Extractor.FLAG_ENABLE_INDEX_SEEKING)

        val exoPlayer = SimpleExoPlayer.Builder(this).apply {
            setMediaSourceFactory(DefaultMediaSourceFactory(this@PlayerService, extractorsFactory))
            //player.setWakeMode(C.WAKE_MODE_LOCAL)
        }.build()

        val audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioAttributes = AudioAttributesCompat.Builder()
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .build()
        player = AudioFocusWrapper(
            audioAttributes,
            audioManager,
            exoPlayer
        ).apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if(!isPlaying) savePosition()
                }
            })
        }

        /** Build a notification manager for our player, set a notification listener to this,
         * and assign the player just created.
         *
         * It is very important to note we need to get a [PlayerNotificationManager] instance
         * via the [PlayerNotificationManager.createWithNotificationChannel] because targeting Android O+
         * when building a notification we need to create a channel to which assign it.
         */
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL
        ).apply {
            setChannelNameResourceId(R.string.app_name)
            setSmallIconResourceId(R.drawable.ic_notification)
            setMediaDescriptionAdapter(DescriptionAdapter(this@PlayerService))
            setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                /** NotificationListener callbacks, we get these calls when our [playerNotificationManager]
                 * dispatches them, subsequently to our [PlayerNotificationManager.setPlayer] call.
                 *
                 * This way we can make our service a foreground service given a notification which was built
                 * [playerNotificationManager].
                 */
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopSelf()
                }

                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    if (ongoing) startForeground(notificationId, notification)
                    else stopForeground(false)
                }
            })
        }.build().apply {
            //setOngoing(true)
            setUsePreviousAction(false)
            setUseNextAction(false)
            //setUseStopAction(true)
        }.also {
            it.setPlayer(player)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return PlayerServiceBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stop()

        this.bookId = intent?.getLongExtra("_id", -1) ?: -1
        start()

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
        playerNotificationManager.setPlayer(null)
        // Destroy the player instance.
        player.release() // player instance can't be used again.
        playerState = null
    }

    // Prepare playback.
    fun start() {
        val book = Book.findById(this, bookId) ?: return
        playerState = PlayerState(position = book.lastReadPosition())
        with(player) {
            // Restore state (after onResume()/onStart())
            val originalMediaItem = MediaItem.fromUri(book.uri())
            val mediaItem = originalMediaItem.buildUpon().apply {
                setMediaMetadata(originalMediaItem.mediaMetadata.buildUpon().apply {
                    setTitle(book.title())
                }.build())
            }.build()

            setMediaItem(mediaItem)
            prepare()
            playerState?.run {
                // Start playback when media has buffered enough
                // (whenReady is true by default).
                playWhenReady = whenReady
                seekTo(window, position)
            }
        }
    }

    // Stop playback and release resources, but re-use the player instance.
    private fun stop() {
        savePosition()
        with(player) {
            // Save state
            playerState?.run {
                position = currentPosition
                window = currentWindowIndex
                whenReady = playWhenReady
            }
            // Stop the player (and release it's resources). The player instance can be reused.
            stop()
            clearMediaItems()
        }
    }

    inner class PlayerServiceBinder : Binder() {
        fun getPlayerInstance() = player
    }

    override fun onHandleIntent(intent: Intent?) {}

    private fun savePosition() {
        if (bookId == -1L) return;
        val position = player.currentPosition
        val lastReadPosition = if (position == C.TIME_UNSET || position >= player.duration) 0 else position
        val book = Book.findById(this@PlayerService, bookId)
        book.lastReadPosition(lastReadPosition)
        book.save(this@PlayerService)
    }
}
