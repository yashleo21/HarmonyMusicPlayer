package com.example.harmonymusicplayer.mediaplayer

import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.harmonymusicplayer.R
import com.example.harmonymusicplayer.interfaces.OnSongClicked
import kotlinx.android.synthetic.main.song_item.view.*

class MediaPlayerAdapter(val onSongClicked: OnSongClicked): RecyclerView.Adapter<MediaPlayerAdapter.ViewHolder>() {
    val songList = mutableListOf<MediaBrowserCompat.MediaItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false))
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.songName.text = songList[position].description.title
        holder.songName.setOnClickListener {
            onSongClicked.songClick(songList[position])
        }
    }


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val songName = view.tv_song_name
    }
}