package com.example.harmonymusicplayer.mediaplayer.library

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.harmonymusicplayer.BuildConfig
import com.example.harmonymusicplayer.MediaPlayerActivity
import com.example.harmonymusicplayer.R
import java.util.concurrent.TimeUnit

const val READ_EXTERNAL_PERMISSION_REQUEST = 945
class MusicLibraryFromPhone(val context: Context) {

    val music = mutableMapOf<String, MediaMetadataCompat>()
    val albumRes = mutableMapOf<String, Int>()
    val musicFilename = mutableMapOf<String, String>()
    val tempSongList = mutableListOf<Song>()

    init {
        getSongList()
    }
    fun getSongList() {
        Log.d("Emre1s", "This was called. music library from phone")
        val musicContentResolver = context.contentResolver
        val musicUriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicUriInternal = MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        val musicCursorExternalSource = musicContentResolver.query(musicUriExternal, null, null,
            null, null)
        val musicCursorInternalSource = musicContentResolver.query(musicUriInternal, null, null,
            null, null)

        if (musicCursorExternalSource != null && musicCursorExternalSource.moveToFirst()) {
            val titleColumn = musicCursorExternalSource.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursorExternalSource.getColumnIndex(MediaStore.Audio.Media._ID)
            val artistColumn = musicCursorExternalSource.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumName = musicCursorExternalSource.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val albumId = musicCursorExternalSource.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val data = musicCursorExternalSource.getColumnIndex(MediaStore.Audio.Media.DATA)
            val albumKey = musicCursorExternalSource.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY)
            val mediaDurationKey = musicCursorExternalSource.getColumnIndex(MediaStore.Audio.Media.DURATION)

            do {
                val thisId = musicCursorExternalSource.getLong(idColumn)
                val thisTitle = musicCursorExternalSource.getString(titleColumn)
                val thisArtist = musicCursorExternalSource.getString(artistColumn)
                val thisalbumId = musicCursorExternalSource.getLong(albumId)
                val thisdata = musicCursorExternalSource.getString(data)
                val albumKeyVal = musicCursorExternalSource.getString(albumKey)
                val albumName = musicCursorExternalSource.getString(albumName)
                val mediaDuration = musicCursorExternalSource.getLong(mediaDurationKey)

                val songItem = Song(thisId, thisTitle, thisArtist, thisalbumId, thisdata, albumKeyVal)
                Log.d("Emre1s", "Songs: $songItem")
                tempSongList.add(songItem)
                createMediaMetadataCompat(thisId.toString(), thisTitle, thisArtist, albumName, "unavailable", mediaDuration, TimeUnit.MILLISECONDS,
                    thisdata, R.drawable.album_jazz_blues, albumKeyVal)
            } while (musicCursorExternalSource.moveToNext())
            musicCursorExternalSource.close()
        }
        else {
            Log.d("Emre1s", "Fuckmusic cursor is null?!")
        }
//
//        if (musicCursorInternalSource != null && musicCursorInternalSource.moveToFirst()) {
//            val titleColumn = musicCursorInternalSource.getColumnIndex(MediaStore.Audio.Media.TITLE)
//            val idColumn = musicCursorInternalSource.getColumnIndex(MediaStore.Audio.Media._ID)
//            val artistColumn = musicCursorInternalSource.getColumnIndex(MediaStore.Audio.Media.ARTIST)
//            val albumId = musicCursorInternalSource.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
//            val albumName = musicCursorInternalSource.getColumnIndex(MediaStore.Audio.Media.ALBUM)
//            val path = musicCursorInternalSource.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
//            val data = musicCursorInternalSource.getColumnIndex(MediaStore.Audio.Media.DATA)
//            val albumKey = musicCursorInternalSource.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY)
//            val mediaDurationKey = musicCursorInternalSource.getColumnIndex(MediaStore.Audio.Media.DURATION)
//
//            do {
//                val thisId = musicCursorInternalSource.getLong(idColumn)
//                val thisTitle = musicCursorInternalSource.getString(titleColumn)
//                val thisArtist = musicCursorInternalSource.getString(artistColumn)
//                val thisalbumId = musicCursorInternalSource.getLong(albumId)
//                val thisdata = musicCursorInternalSource.getString(data)
//                val albumKeyVal = musicCursorInternalSource.getString(albumKey)
//                val albumName = musicCursorInternalSource.getString(albumName)
//                val mediaDuration = musicCursorInternalSource.getLong(mediaDurationKey)
//
//                val songItem = Song(thisId, thisTitle, thisArtist, thisalbumId, thisdata, albumKeyVal)
//                Log.d("Emre1s", " INTERNAL Songs: $songItem and path: $path")
//                tempSongList.add(songItem)
//                createMediaMetadataCompat(thisId.toString(), thisTitle, thisArtist, albumName, "unavailable", mediaDuration, TimeUnit.MILLISECONDS,
//                    thisdata, R.drawable.album_jazz_blues, albumKeyVal)
//            }
//            while (musicCursorInternalSource.moveToNext())
//
//            musicCursorInternalSource.close()
//        } else {
//            Log.d("Emre1s", "Fuckmusic cursorinternal is null?!")
//        }

    }

    private fun createMediaMetadataCompat(mediaId: String, title: String, artist: String, album: String, genre: String,
                                          duration: Long, durationUnit: TimeUnit, musicFilename: String,
                                          albumArtResId: Int, albumArtResName: String){
        Log.d("Emre1s", "Media id: $mediaId")
        music[mediaId] = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                TimeUnit.MILLISECONDS.convert(duration, durationUnit))
            .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
            .putString(
                MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                getAlbumArtUri(albumArtResName)
            )
            .putString(
                MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                getAlbumArtUri(albumArtResName)
            )
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, musicFilename)
            .build()

        albumRes[mediaId] = albumArtResId
        this.musicFilename[mediaId] = musicFilename
    }

    private fun getAlbumArtUri(albumArtResName: String): String {
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                BuildConfig.APPLICATION_ID + "/drawable/" + albumArtResName
    }

    fun getRoot(): String {
        return "root"
    }

    fun getAlbumUri(albumArtRes: String): String {
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                BuildConfig.APPLICATION_ID + "/drawable/" + albumArtRes
    }

    fun getMusicFilename(mediaId: String): String {
        return musicFilename[mediaId] ?: "Song not found"
    }

    fun getAlbumRes(mediaId: String): Int {
        return R.drawable.album_jazz_blues
    }

    fun getAlbumBitmap(context: Context, mediaId: String): Bitmap {
        return BitmapFactory.decodeResource(context.resources, getAlbumRes(mediaId))
    }

    fun getMediaItems(): MutableList<MediaBrowserCompat.MediaItem> {
        val result = mutableListOf<MediaBrowserCompat.MediaItem>()
        music.values.forEach {
            result.add(MediaBrowserCompat.MediaItem(it.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
        }
        return result
    }

    fun getMetadata(context: Context, mediaId: String): MediaMetadataCompat {
        val metadataWithoutBitmap = music[mediaId]
        val albumArt = getAlbumBitmap(context, mediaId)

        // Since MediaMetadataCompat is immutable, we need to create a copy to set the album art.
        // We don't set it initially on all items so that they don't take unnecessary memory.
        val builder = MediaMetadataCompat.Builder()
        for (key in arrayOf(
            MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
            MediaMetadataCompat.METADATA_KEY_ALBUM,
            MediaMetadataCompat.METADATA_KEY_ARTIST,
            MediaMetadataCompat.METADATA_KEY_GENRE,
            MediaMetadataCompat.METADATA_KEY_TITLE,
            MediaMetadataCompat.METADATA_KEY_MEDIA_URI
        )) {
            builder.putString(key, metadataWithoutBitmap!!.getString(key))
        }
        builder.putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION,
            metadataWithoutBitmap!!.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
        )
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
        return builder.build()
    }
}