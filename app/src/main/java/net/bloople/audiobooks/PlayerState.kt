package net.bloople.audiobooks

data class PlayerState(
    var window: Int = 0,
    var position: Long = 0,
    var whenReady: Boolean = true
)