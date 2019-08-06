package com.example.harmonymusicplayer.mediaplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NoisyMediaReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Emre1s", "Media became nooisy.")
    }
}