package com.dynamicisland.app

import android.graphics.Bitmap

data class MediaData(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val albumArt: Bitmap? = null,
    val isPlaying: Boolean = false,
    val sessionToken: Any? = null  // MediaSession.Token
)