/*
 * Copyright 2018 Google LLC. All rights reserved.
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


package net.bloople.audiobooks.player

import android.content.Context
import android.media.AudioManager
import androidx.media.AudioAttributesCompat
import com.google.android.exoplayer2.*

/**
 * Creates and manages a [com.google.android.exoplayer2.ExoPlayer] instance.
 */
class PlayerHolder(
    context: Context,
    private val streamUrl: String,
    private val streamPosition: Long,
    private val playerState: PlayerState
) {
    val audioFocusPlayer: ExoPlayer

    // Create the player instance.
    init {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioAttributes = AudioAttributesCompat.Builder()
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .build()
        audioFocusPlayer = AudioFocusWrapper(
            audioAttributes,
            audioManager,
            SimpleExoPlayer.Builder(context).apply {

            }.build()
        ).apply {
            setMediaItem(MediaItem.fromUri(streamUrl))
            seekTo(streamPosition)
            prepare()
        }
        println("SimpleExoPlayer created")
    }

    // Prepare playback.
    fun start() {
        with(audioFocusPlayer) {
            // Restore state (after onResume()/onStart())
            setMediaItem(MediaItem.fromUri(streamUrl))
            prepare()
            with(playerState) {
                // Start playback when media has buffered enough
                // (whenReady is true by default).
                playWhenReady = whenReady
                seekTo(window, position)
                // Add logging.
                attachLogging(audioFocusPlayer)
            }
            println("SimpleExoPlayer is started")
        }
    }

    // Stop playback and release resources, but re-use the player instance.
    fun stop() {
        with(audioFocusPlayer) {
            // Save state
            with(playerState) {
                position = currentPosition
                window = currentWindowIndex
                whenReady = playWhenReady
            }
            // Stop the player (and release it's resources). The player instance can be reused.
            stop()
            clearMediaItems()
        }
        println("SimpleExoPlayer is stopped")
    }

    // Destroy the player instance.
    fun release() {
        audioFocusPlayer.release() // player instance can't be used again.
        println("SimpleExoPlayer is released")
    }

    /**
     * For more info on ExoPlayer logging, please review this
     * [codelab](https://codelabs.developers.google.com/codelabs/exoplayer-intro/#5).
     */
    private fun attachLogging(exoPlayer: ExoPlayer) {
        // Write to log on state changes.
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                println("playerStateChanged: ${getStateString(playbackState)}, $playWhenReady")
            }

            override fun onPlayerError(error: PlaybackException) {
                println("playerError: $error")
            }

            fun getStateString(state: Int): String {
                return when (state) {
                    Player.STATE_BUFFERING -> "STATE_BUFFERING"
                    Player.STATE_ENDED -> "STATE_ENDED"
                    Player.STATE_IDLE -> "STATE_IDLE"
                    Player.STATE_READY -> "STATE_READY"
                    else -> "?"
                }
            }
        })
    }
}