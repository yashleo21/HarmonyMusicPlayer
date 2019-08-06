package com.example.harmonymusicplayer.interfaces

import android.support.v4.media.MediaBrowserCompat

interface OnSongClicked {
    fun songClick(songItem: MediaBrowserCompat.MediaItem)
}