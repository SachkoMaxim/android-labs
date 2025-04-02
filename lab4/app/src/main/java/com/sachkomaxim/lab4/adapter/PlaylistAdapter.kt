package com.sachkomaxim.lab4.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sachkomaxim.lab4.R
import com.sachkomaxim.lab4.model.Song

class PlaylistAdapter(
    private var songs: List<Song>,
    private val onItemClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.SongViewHolder>() {

    private var currentPlayingPosition = -1

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.songTitleTextView)
        val sourceTextView: TextView = view.findViewById(R.id.songSourceTextView)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.titleTextView.text = song.title
        holder.sourceTextView.text = "${song.storageType}: ${song.path}"

        if (position == currentPlayingPosition) {
            holder.itemView.setBackgroundResource(R.color.purple_light)
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent)
        }

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }

        holder.deleteButton.setOnClickListener {
            val songPosition = holder.adapterPosition
            if (songPosition != RecyclerView.NO_POSITION) {
                onDeleteClick(songPosition)
            }
        }
    }

    override fun getItemCount() = songs.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    fun setCurrentPlayingPosition(position: Int) {
        val oldPosition = currentPlayingPosition
        currentPlayingPosition = position

        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
        if (currentPlayingPosition != -1) {
            notifyItemChanged(currentPlayingPosition)
        }
    }
}
