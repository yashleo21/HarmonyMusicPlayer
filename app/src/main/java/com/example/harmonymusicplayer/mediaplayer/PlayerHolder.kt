package com.example.harmonymusicplayer.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.annotation.RawRes
import java.lang.Exception
import java.util.concurrent.Executors

class PlayerHolder(val context: Context) : MediaPlayer(){

    override fun start() {
        Log.d("Emre1s", "Media player start called")
        super.start()
    }

    override fun pause() {
        Log.d("Emre1s", "Media player on paused called")
        super.pause()
    }



    fun playFromMedia(song: MediaMetadataCompat?) {
        val mediaId = song?.description?.mediaId
       // val mediaString = MusicLibrary.getMusicFilename(mediaId!!)
     //   Log.d("Emre1s", "Received player holder: $mediaString")
    }

    fun playFromMedia(song: String?) {
        Log.d("Emre1s", "Son received: $song")
        reset()
        try {
            setDataSource(context, Uri.parse(song))
        } catch (e: Exception) {
            Log.d("Emre1s", "Playfrom media aexception ${e.message}")
        }
//        Executors.newSingleThreadExecutor().execute {
////            prepareAsync()
////        }
        prepare()
        Log.d("Emre1s", "What to do? $isPlaying")
    }

//    fun playSound(@RawRes rawResId: Int) {
//        val assetFileDescriptor = context.resources.openRawResourceFd(rawResId) ?: return
//        Log.d("Emre1s", "${assetFileDescriptor.fileDescriptor}  ${assetFileDescriptor.startOffset}   ${assetFileDescriptor.declaredLength}")
//        run {
//            reset()
//            try {
//                setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength)
//            }
//            catch (e: Exception) {
//                Log.d("Emre1s", "Playerholder playsound exception ${e.message}")
//            }
//
//            prepare()
//            start()
//            Log.d("Emre1s", "What to do? $isPlaying")
//        }
//    }

    override fun setOnCompletionListener(listener: OnCompletionListener?) {
        Log.d("Emre1s", "Complete? hahahahaha")
        listener?.onCompletion(this)
    }

    override fun setOnPreparedListener(listener: OnPreparedListener?) {
        Log.d("Emre1s", "onprepared called")
        super.setOnPreparedListener(listener)
    }
}