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
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.metadata.Metadata
import net.bloople.audiobooks.media.DescriptionAdapter
import net.bloople.audiobooks.player.PlayerHolder
import net.bloople.audiobooks.player.PlayerState
import com.google.android.exoplayer2.ui.PlayerNotificationManager

/**
 * Created by Filippo Beraldo on 15/11/2018.
 * http://github.com/beraldofilippo
 */
class PlayerService : IntentService("audiobooks") {
    companion object {
        const val NOTIFICATION_ID = 100
        const val NOTIFICATION_CHANNEL = "audiobooks_channel"
    }

    private var bookId: Long = -1

    private lateinit var playerHolder: PlayerHolder

    private lateinit var playerNotificationManager: PlayerNotificationManager

    override fun onBind(intent: Intent?): IBinder {
        this.bookId = intent?.getLongExtra("_id", -1) ?: -1
        initialize()

        return PlayerServiceBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        savePosition()
        playerNotificationManager.setPlayer(null)
        playerHolder.release()
    }

    private fun initialize() {
        val book = Book.findById(this, bookId) ?: return

        // Build a player holder
        playerHolder = PlayerHolder(this, book.uri(), book.title(), PlayerState(position = book.lastReadPosition()))

        playerHolder.audioFocusPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if(!isPlaying) savePosition()
            }
        })

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
            setMediaDescriptionAdapter(DescriptionAdapter(this@PlayerService, bookId))
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
            setUseStopAction(true)
        }.also {
            it.setPlayer(playerHolder.audioFocusPlayer)
        }

        playerHolder.start()
    }

    inner class PlayerServiceBinder : Binder() {
        fun getPlayerHolderInstance() = playerHolder
    }

    override fun onHandleIntent(intent: Intent?) {}

    private fun savePosition() {
        val player = playerHolder.audioFocusPlayer
        val currentReadPosition = if (player.getPlaybackState() == Player.STATE_ENDED) 0 else player.getCurrentPosition()
        val book = Book.findById(this@PlayerService, bookId)
        book.lastReadPosition(currentReadPosition)
        book.save(this@PlayerService)
    }
}
