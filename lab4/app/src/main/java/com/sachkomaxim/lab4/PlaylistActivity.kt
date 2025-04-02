package com.sachkomaxim.lab4

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sachkomaxim.lab4.adapter.PlaylistAdapter
import com.sachkomaxim.lab4.model.Song
import com.sachkomaxim.lab4.service.MusicService
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar

class PlaylistActivity : AppCompatActivity() {

    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var nowPlayingTextView: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var stopButton: ImageButton

    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            updateUI()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        playlistRecyclerView = findViewById(R.id.playlistRecyclerView)
        nowPlayingTextView = findViewById(R.id.nowPlayingTextView)
        playPauseButton = findViewById(R.id.playPauseButton)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
        stopButton = findViewById(R.id.stopButton)

        playlistAdapter = PlaylistAdapter(
            emptyList(),
            { position ->
                musicService?.playAt(position)
                updateUI()
            },
            { position ->
                musicService?.let { service ->
                    val song = service.getPlaylist()[position]
                    service.removeSongFromPlaylist(position)
                    updateUI()

                    Snackbar.make(
                        playlistRecyclerView,
                        "Removed: ${song.title}",
                        Snackbar.LENGTH_LONG
                    ).setAction("UNDO") {
                        musicService?.addToPlaylist(song)
                        updateUI()
                    }.show()
                }
            }
        )

        playlistRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlaylistActivity)
            adapter = playlistAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                musicService?.let { service ->
                    if (position >= 0 && position < service.getPlaylist().size) {
                        val song = service.getPlaylist()[position]
                        service.removeSongFromPlaylist(position)
                        updateUI()

                        Snackbar.make(
                            playlistRecyclerView,
                            "Removed: ${song.title}",
                            Snackbar.LENGTH_LONG
                        ).setAction("UNDO") {
                            service.addToPlaylist(song)
                            updateUI()
                        }.show()
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(playlistRecyclerView)

        setupButtonListeners()
        bindMusicService()
    }

    private fun setupButtonListeners() {
        playPauseButton.setOnClickListener {
            if (musicService?.isPlaying() == true) {
                musicService?.pauseMusic()
            } else {
                musicService?.resumeMusic()
            }
            updateUI()
        }

        previousButton.setOnClickListener {
            musicService?.playPrevious()
            updateUI()
        }

        nextButton.setOnClickListener {
            musicService?.playNext()
            updateUI()
        }

        stopButton.setOnClickListener {
            musicService?.stopMusic()
            updateUI()
        }
    }

    private fun bindMusicService() {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        startService(intent)
    }

    private fun updateUI() {
        musicService?.let { service ->
            val playlist = service.getPlaylist()
            playlistAdapter.updateSongs(playlist)

            val currentSongIndex = service.getCurrentSongIndex()
            playlistAdapter.setCurrentPlayingPosition(currentSongIndex)

            val currentSong = service.getCurrentSong()
            nowPlayingTextView.text = currentSong?.title ?: "No song playing"

            if (service.isPlaying()) {
                playPauseButton.setImageResource(R.drawable.round_pause_24)
            } else {
                playPauseButton.setImageResource(R.drawable.round_play_arrow_24)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isBound) {
            updateUI()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        super.onDestroy()
    }

    companion object {
        fun addSongToPlaylist(context: Context, song: Song) {
            val intent = Intent(context, MusicService::class.java)
            context.startService(intent)

            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as MusicService.MusicBinder
                    val musicService = binder.getService()
                    musicService.addToPlaylist(song)
                    Toast.makeText(context, "Added to playlist: ${song.title}", Toast.LENGTH_SHORT).show()
                    context.unbindService(this)
                }

                override fun onServiceDisconnected(name: ComponentName?) {}
            }

            context.bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }
}
