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
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory

/**
 * Creates and manages a [com.google.android.exoplayer2.ExoPlayer] instance.
 */
class PlayerHolder(
    context: Context,
    private val mediaUrl: String,
    private val mediaTitle: String,
    private val playerState: PlayerState
) {
    val audioFocusPlayer: ExoPlayer
    private val mediaItem: MediaItem

    // Create the player instance.
    init {
        val originalMediaItem = MediaItem.fromUri(mediaUrl)
        mediaItem = originalMediaItem.buildUpon().apply {
            setMediaMetadata(originalMediaItem.mediaMetadata.buildUpon().apply {
                setTitle(mediaTitle)
            }.build())
        }.build()

        val extractorsFactory: DefaultExtractorsFactory = DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)
            .setMp3ExtractorFlags(Mp3Extractor.FLAG_ENABLE_INDEX_SEEKING)

        val player = SimpleExoPlayer.Builder(context).apply {
            setMediaSourceFactory(DefaultMediaSourceFactory(context, extractorsFactory))
            //player.setWakeMode(C.WAKE_MODE_LOCAL)
        }.build()

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioAttributes = AudioAttributesCompat.Builder()
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .build()
        audioFocusPlayer = AudioFocusWrapper(
            audioAttributes,
            audioManager,
            player
        ).apply {
            setMediaItem(mediaItem)
            prepare()
        }
    }

    // Prepare playback.
    fun start() {
        with(audioFocusPlayer) {
            // Restore state (after onResume()/onStart())
            setMediaItem(mediaItem)
            prepare()
            with(playerState) {
                // Start playback when media has buffered enough
                // (whenReady is true by default).
                playWhenReady = whenReady
                seekTo(window, position)
            }
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
    }

    // Destroy the player instance.
    fun release() {
        audioFocusPlayer.release() // player instance can't be used again.
    }
}