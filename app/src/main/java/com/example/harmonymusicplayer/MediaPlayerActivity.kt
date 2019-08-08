package com.example.harmonymusicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.harmonymusicplayer.interfaces.OnSongClicked
import com.example.harmonymusicplayer.mediaplayer.MediaPlaybackService
import com.example.harmonymusicplayer.mediaplayer.MediaPlayerAdapter
import com.example.harmonymusicplayer.mediaplayer.library.MusicLibraryFromPhone
import com.example.harmonymusicplayer.mediaplayer.library.READ_EXTERNAL_PERMISSION_REQUEST
import kotlinx.android.synthetic.main.activity_main.*

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var mediaToken: MediaSessionCompat.Token
    private lateinit var transportControls: MediaControllerCompat.TransportControls
    private lateinit var mediaController: MediaControllerCompat
    private val mediaAdapter by lazy {
        MediaPlayerAdapter(object : OnSongClicked {
            override fun songClick(songItem: MediaBrowserCompat.MediaItem) {
                Log.d("Emre1s", "Song clicked ${songItem.mediaId}")
              //  mediaBrowser.getItem(songItem.mediaId ?: "1", mediaItemCallback)
                val bundle = Bundle()
                bundle.putString("mediaId", songItem.mediaId)
                transportControls.playFromMediaId(songItem.description.mediaUri.toString(), bundle)
            }
        })
    }
    private lateinit var musicLibraryFromPhone: MusicLibraryFromPhone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ( ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_PERMISSION_REQUEST)
            } else {
                Log.d("Emre1s", "Why am i called so many times")
            }
        }
       // musicLibraryFromPhone = MusicLibraryFromPhone(this@MediaPlayerActivity)
        rv_songs.adapter = mediaAdapter
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            connectionCallbacks,
            null
        )
        mediaBrowser.connect()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        //mediaController.unregisterCallback(controllerCallback)
//        mediaBrowser.unsubscribe(MediaPlayerActivity::class.simpleName ?: "MediaPlayerActivity")
//        mediaBrowser.disconnect()
    }

    fun buildTransportControls() {

        play_pause.setOnClickListener {
            val playbackState = mediaController.playbackState.state
            Log.d("Emre1s", "Playback state is: $playbackState")
            when (play_pause.text) {
                "Play" -> {
                   // play_pause.text = "Pause"
                    transportControls.play()
                }
                "Pause" -> {
                  //  play_pause.text = "Play"
                    transportControls.pause()
                }
            }
        }

//        val metadata = mediaController.metadata
//        val pbState = mediaController.playbackState
    }

    private val connectionCallbacks = object: MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaToken = mediaBrowser.sessionToken
            Log.d("Emre1s", "Media browser connection successful $mediaToken")
            mediaController = MediaControllerCompat(this@MediaPlayerActivity, mediaToken)
            mediaController.registerCallback(controllerCallback)
            mediaBrowser.subscribe(mediaBrowser.root
                , subscriptionCallback)
            transportControls = mediaController.transportControls

            buildTransportControls()
        }

        override fun onConnectionSuspended() {
            Log.d("Emre1s", "oonnection suspended")
            super.onConnectionSuspended()
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            Log.d("Emre1s", "oonnection failure")
            // The Service has refused our connection
        }

    }

    private var controllerCallback  = object: MediaControllerCompat.Callback() {  //TODO: Hook this up
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            Log.d("Emre1s", "Controller callback: ${metadata?.description?.title} metadatachanged")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            Log.d("Emre1s", "Playback callback: ${state?.playbackState?.toString()}")
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    Log.d("Emre1s", "I rain?")
                    play_pause.text = "Pause"
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    play_pause.text = "Play"
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    play_pause.text = "Play"
                }
            }
        }
    }

    private var subscriptionCallback = object: MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
            Log.d("Emre1s", "Subscription callback called on child loaded $parentId")
            mediaAdapter.songList.addAll(children)
            mediaAdapter.notifyDataSetChanged()
            children.forEach {
                Log.d("Emre1s", "Children Loaded: ${it.description.title}")
            }
        }

        override fun onError(parentId: String) {
            Log.d("Emre1s", "Subscription callback called on child loaded $parentId")
            Log.d("Emre1s", "subscripton error: $parentId")
        }
    }

    private var mediaItemCallback = object : MediaBrowserCompat.ItemCallback() {
        override fun onItemLoaded(item: MediaBrowserCompat.MediaItem?) {
            super.onItemLoaded(item)
        }

        override fun onError(itemId: String) {
            super.onError(itemId)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == READ_EXTERNAL_PERMISSION_REQUEST) {
            Log.d("Emre1s", "This was called")
            if (grantResults[0] == RESULT_OK) {
                musicLibraryFromPhone.getSongList()
            } else {
                startActivityForResult(Intent(Settings.ACTION_APPLICATION_SETTINGS, Uri.fromParts("package", packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), READ_EXTERNAL_PERMISSION_REQUEST)
            }
        }
    }
}
