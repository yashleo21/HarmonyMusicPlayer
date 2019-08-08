package com.example.harmonymusicplayer.mediaplayer

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.*
import android.media.AudioManager.OnAudioFocusChangeListener
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.AudioManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.example.harmonymusicplayer.R
import com.example.harmonymusicplayer.mediaplayer.library.MusicLibraryFromPhone
import java.util.concurrent.TimeUnit

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
private const val MEDIA_BUTTONS = 21
private const val TRANSPORT_CONTROLS = 22
private const val NOTIFICATION_ID = 45
const val CHANNEL_ID = "music-notification"
private lateinit var notificationChannel: NotificationChannel
private val LOG_TAG = MediaPlaybackService::class.simpleName ?: "MediaPlayerService"


/*When the service receives the onCreate() lifecycle callback method it should perform these steps:

Create and initialize the media session
Set the media session callback
Set the media session token*/

class MediaPlaybackService: MediaBrowserServiceCompat() {

    var mediaSession: MediaSessionCompat? = null
    private lateinit var stateCallback: PlaybackStateCompat
    private val mediaPlayer by lazy {
        PlayerHolder(this)
    }

    private val audioManager by lazy {
         getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val stateBuilder by lazy {
        PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE
                    or PlaybackStateCompat.ACTION_STOP)
    }

    private val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

    val focusLock = Any()
    var playbackDelay = false
    var playbackAuthorized = false

    private val musicLibraryFromPhone by lazy {
        MusicLibraryFromPhone(this)
    }

    private val notificationBuilder by lazy {
        MediaStyleHelper.from(this, mediaSession!!)
    }

    val musicNotification by lazy {
        notificationBuilder.run {
            setSmallIcon(R.drawable.ic_launcher_background)
            color = ContextCompat.getColor(this@MediaPlaybackService, R.color.colorPrimary)

            addAction(
                androidx.core.app.NotificationCompat.Action(
                    R.drawable.ic_pause_black_24dp,
                    getString(R.string.pause_text),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaPlaybackService,
                        PlaybackStateCompat.ACTION_PAUSE
                    )
                )
            )

            setStyle(
                NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0)
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaPlaybackService,
                        PlaybackStateCompat.ACTION_STOP)
                    ))

            build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, LOG_TAG).apply {

            stateCallback = stateBuilder.build()
            setPlaybackState(stateCallback)
            setCallback(MediaSessionCallback()) //1.
            setSessionToken(sessionToken) //2. 3. is not necessary
            isActive = true
        } //MediaSession initialization
        musicLibraryFromPhone
        //mediaPlayer.setOnCompletionListener(mediaPlaybackComplete)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        //TODO: Stop player adapter/playback once I actully create it
        mediaPlayer.stop()
        mediaSession?.release()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        // Just making it simple by allowing every package name to access my service for now
        Log.d("Emre1s", "onGetRoot called")
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.d("Emre1s", "onLoadChildren was called $parentId")
        if (MY_EMPTY_MEDIA_ROOT_ID == parentId) {
            result.sendResult(null)
            return
        }

        if (MY_MEDIA_ROOT_ID == parentId) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
            mediaItems.addAll(musicLibraryFromPhone.getMediaItems())

        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems)
    }

    override fun onLoadItem(itemId: String?, result: Result<MediaBrowserCompat.MediaItem>) {
        //result.sendResult()
        super.onLoadItem(itemId, result)
    }

    @SuppressLint("NewApi")
    inner class MediaSessionCallback: MediaSessionCompat.Callback(), OnAudioFocusChangeListener  {

        private val mPlaylist = arrayListOf<MediaSessionCompat.QueueItem>()
        private var mQueueIndex = -1
        private var mPreparedMedia: MediaMetadataCompat? = null
        private val noisyReceiver by lazy {
            NoisyMediaReceiver()
        }

        private val focusRequest by lazy {
                    AudioFocusRequest.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN).run {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        setAcceptsDelayedFocusGain(true)
                        setOnAudioFocusChangeListener(this@MediaSessionCallback, handler)
                        build()
                    }
        }



        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            Log.d("Emre1s", "onAddquuee item??")
            mPlaylist.add(MediaSessionCompat.QueueItem(description, description.hashCode().toLong()))
            mQueueIndex =  if (mQueueIndex == -1) 0 else mQueueIndex
            mediaSession?.setQueue(mPlaylist)
        }


        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            mPlaylist.remove(MediaSessionCompat.QueueItem(description, description.hashCode().toLong()))
            mQueueIndex = if (mPlaylist.isEmpty()) -1 else mQueueIndex
            mediaSession?.setQueue(mPlaylist)
        }

        override fun onPrepare() {
            if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
                return //no song to play
            }
            val mediaId = mPlaylist[mQueueIndex].description.mediaId
            mPreparedMedia = musicLibraryFromPhone.getMetadata(context = this@MediaPlaybackService, mediaId = mediaId ?: "none")
            mediaSession?.setMetadata(mPreparedMedia)

            if (mediaSession?.isActive == false) {
                mediaSession?.isActive = true
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Log.d("Emre1s", "On play from media called $mediaId")
            val mediaIden = extras?.getString("mediaId")
            if (mediaSession?.isActive == false) {
                mediaSession?.isActive = true
            }
            Log.d("Emre1s", "Playback staatus: ${mediaSession?.controller?.playbackState?.state}")
            val result = createAudioFocusRequest()
            synchronized(focusLock) {
                playbackAuthorized = when (result) {
                    AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                        Log.d("Emre1s", "Audio focus failed")
                        false
                    }
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                        //Start playback
                        mediaPlayer.playFromMedia(mediaId, mediaPlaybackComplete)
                        //mediaPlayer.setOnCompleteListener(mediaPlaybackComplete)
                        mediaSession?.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            0L, 1f).build())
                        mediaSession?.isActive = true
                        mediaSession?.setMetadata(musicLibraryFromPhone.getMetadata(this@MediaPlaybackService, mediaIden!!))
                        Log.d("Emre1s", "Audio focus granted")
                        onPlay()
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            createNotificationChannel()
                            notificationManager.createNotificationChannel(notificationChannel)
                        }
                        musicNotification.actions[0] = Notification.Action(R.drawable.ic_pause, getString(R.string.pause_text),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this@MediaPlaybackService,
                            PlaybackStateCompat.ACTION_PAUSE
                        ))

                        startForeground(NOTIFICATION_ID, musicNotification)
                        true
                    }
                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                        playbackDelay = true
                        Log.d("Emre1s", "Audio focus delayed")
                        false
                    }
                    else -> false
                }
                Log.d("Emre1s", "Playbackauthorized value $playbackAuthorized")
            }
        }

        private fun createAudioFocusRequest(): Int {
            return if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.requestAudioFocus(focusRequest)
            } else {
                audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }
            }

        override fun onPlay() {

            mediaPlayer.start()
            val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            registerReceiver(noisyReceiver, intentFilter)
            Log.d("Emre1s", "onPlayFromMediaId: MediaSession active")
            mediaSession?.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                0L, 1f).build())
        }

        private fun isReadyToPlay(): Boolean {
            return mPlaylist.isNotEmpty()
        }

        override fun onPause() {
            stopForeground(false)
            Log.d("Emre1s", "onPause is called")
            unregisterReceiver(noisyReceiver)
            mediaPlayer.pause()
            mediaSession?.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED,0L, 1f ).build())
        }

        override fun onStop() {
            stopForeground(true)
            audioManager.abandonAudioFocusRequest(focusRequest)
            mediaPlayer.stop()
            mediaSession?.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                0L, 1f).build())
            mediaSession?.isActive = false
        }

        override fun onSkipToNext() {
            mQueueIndex = ++mQueueIndex % mPlaylist.size
            mPreparedMedia = null
            mediaSession?.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                0L, 1f).build())
            onPlay()
        }

        override fun onSkipToPrevious() {
            mQueueIndex = if (mQueueIndex > 0) mQueueIndex - 1 else mPlaylist.size - 1
            mPreparedMedia = null
            mediaSession?.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS,
                0L, 1f).build())
            onPlay()
        }

        override fun onSeekTo(pos: Long) {
            mediaPlayer.seekTo(pos.toInt())
        }

        override fun onAudioFocusChange(focusChange: Int) {
           Log.d("Emre1s", "Audiofocuschange : $focusChange")
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    Log.d("Emre1s", "Focus is gained")
                    onPlay()
                }

                AudioManager.AUDIOFOCUS_LOSS -> {
                    onPause()
                    handler.postDelayed(delayedStopRunnable, TimeUnit.SECONDS.toMillis(30))
                    //pause playback
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    onPause()
                    // pause playback
                }
            }
        }

        val handler = Handler ()
        val delayedStopRunnable = Runnable {
            onStop()
        }
    }

    private val mediaPlaybackComplete = MediaPlayer.OnCompletionListener {
        Log.d("Emre1s", "Media Playback is complete")
        musicNotification.actions[0] = Notification.Action(R.drawable.ic_play, getString(R.string.play_media), MediaButtonReceiver.buildMediaButtonPendingIntent(
            this@MediaPlaybackService,
            PlaybackStateCompat.ACTION_PLAY
        ))
        startForeground(NOTIFICATION_ID, musicNotification)
        mediaSession?.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0L, 1f).build())
    }

    fun createNotificationChannel() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a NotificationChannel
            notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Music notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }
    }
}