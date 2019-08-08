package com.example.harmonymusicplayer.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import java.lang.Exception

class PlayerHolder(val context: Context) {

    var mediaPlayer: MediaPlayer = MediaPlayer()

    fun start() {
        Log.d("Emre1s", "Media player start called")
        mediaPlayer.start()
    }

    fun pause() {
        Log.d("Emre1s", "Media player on paused called")
        mediaPlayer.pause()
    }

    fun seekTo(pos: Int) {
        mediaPlayer.seekTo(pos)
    }

    fun playFromMedia(song: MediaMetadataCompat?) {
        val mediaId = song?.description?.mediaId
       // val mediaString = MusicLibrary.getMusicFilename(mediaId!!)
     //   Log.d("Emre1s", "Received player holder: $mediaString")
    }

    fun playFromMedia(song: String?, listener: MediaPlayer.OnCompletionListener) {
        Log.d("Emre1s", "Son received: $song")
        mediaPlayer.reset()
        try {
            mediaPlayer.setDataSource(context, Uri.parse(song))
        } catch (e: Exception) {
            Log.d("Emre1s", "Playfrom media aexception ${e.message}")
        }

        mediaPlayer.prepare()
        mediaPlayer.setOnCompletionListener(listener)
        Log.d("Emre1s", "What to do? ${mediaPlayer.isPlaying}")
    }

    fun stop() {
        mediaPlayer.stop()
    }

}