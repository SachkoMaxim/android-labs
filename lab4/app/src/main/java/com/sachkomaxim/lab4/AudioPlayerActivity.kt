package com.sachkomaxim.lab4

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.sachkomaxim.lab4.model.Song
import com.sachkomaxim.lab4.service.MusicService
import androidx.core.net.toUri

class AudioPlayerActivity : AppCompatActivity() {

    private val storageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    private lateinit var mediaPlayer: MediaPlayer
    private val songNameTV: TextView by lazy { findViewById(R.id.songNameTV) }
    private val playPauseButton: ImageButton by lazy { findViewById(R.id.playPauseButton) }
    private val stopButton: ImageButton by lazy { findViewById(R.id.stopButton) }

    private var isAudioPaused = true
    private var currentPosition = 0
    private var storageType: String? = null
    private var link: String? = null

    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        storageType = intent.getStringExtra("storageType")
        link = intent.getStringExtra("link")

        if (savedInstanceState != null) {
            isAudioPaused = savedInstanceState.getBoolean("isAudioPaused", true)
            currentPosition = savedInstanceState.getInt("currentPosition", 0)
        }

        val addToPlaylistButton = findViewById<Button>(R.id.addToPlaylistButton)
        addToPlaylistButton.setOnClickListener {
            link?.let { songPath ->
                val fileName = songPath.substringAfterLast("/").ifEmpty { songPath }
                val song = Song(fileName, songPath, storageType ?: MainActivity.STORAGE_URL)

                if (!isBound) {
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent, serviceConnection, BIND_AUTO_CREATE)
                }

                PlaylistActivity.addSongToPlaylist(this, song)

                val playlistIntent = Intent(this, PlaylistActivity::class.java)
                startActivity(playlistIntent)
            }
        }

        initializeMediaPlayer()
        setButtonsOnClickListeners()

        updatePlayPauseButton()
        songNameTV.text = link
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().also {
            it.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            it.setOnCompletionListener {
                isAudioPaused = true
                currentPosition = 0
                updatePlayPauseButton()
            }
        }

        try {
            when (storageType) {
                MainActivity.STORAGE_INTERNAL -> {
                    mediaPlayer.setDataSource(filesDir.absolutePath + "/" + link)
                }
                MainActivity.STORAGE_EXTERNAL -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!Environment.isExternalStorageManager()) {
                            requestPermission()
                        }
                    }
                    mediaPlayer.setDataSource(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + "/" + link)
                }
                else -> {
                    mediaPlayer.setDataSource(this, link!!.toUri())
                }
            }

            mediaPlayer.prepare()

            if (currentPosition > 0) {
                mediaPlayer.seekTo(currentPosition)
                if (!isAudioPaused) {
                    mediaPlayer.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePlayPauseButton() {
        if (isAudioPaused) {
            playPauseButton.setImageResource(R.drawable.round_play_arrow_24)
        } else {
            playPauseButton.setImageResource(R.drawable.round_pause_24)
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            storageActivityResultLauncher.launch(intent)
        }
    }

    private fun setButtonsOnClickListeners() {
        playPauseButton.setOnClickListener {
            if (isAudioPaused) {
                mediaPlayer.start()
            } else {
                mediaPlayer.pause()
                currentPosition = mediaPlayer.currentPosition
            }
            isAudioPaused = !isAudioPaused
            updatePlayPauseButton()
        }

        stopButton.setOnClickListener {
            mediaPlayer.stop()
            mediaPlayer.prepare()
            isAudioPaused = true
            currentPosition = 0
            updatePlayPauseButton()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isAudioPaused", isAudioPaused)
        outState.putInt("currentPosition", if (::mediaPlayer.isInitialized) mediaPlayer.currentPosition else currentPosition)
    }

    override fun onPause() {
        super.onPause()
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            currentPosition = mediaPlayer.currentPosition
            mediaPlayer.pause()
            isAudioPaused = true
            updatePlayPauseButton()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mediaPlayer.isInitialized && !isAudioPaused && currentPosition > 0) {
            mediaPlayer.seekTo(currentPosition)
            mediaPlayer.start()
            updatePlayPauseButton()
        }
    }

    override fun onDestroy() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        super.onDestroy()
    }
}
