package com.sachkomaxim.lab4.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sachkomaxim.lab4.PlaylistActivity
import com.sachkomaxim.lab4.MainActivity
import com.sachkomaxim.lab4.R
import com.sachkomaxim.lab4.model.Song
import androidx.core.net.toUri
import androidx.core.content.edit

class MusicService : Service() {

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentSongIndex = 0
    private var playlist = mutableListOf<Song>()
    private var isPlaying = false
    private var isPaused = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    companion object {
        const val CHANNEL_ID = "MusicServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "com.sachkomaxim.lab4.ACTION_PLAY"
        const val ACTION_PAUSE = "com.sachkomaxim.lab4.ACTION_PAUSE"
        const val ACTION_PREVIOUS = "com.sachkomaxim.lab4.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.sachkomaxim.lab4.ACTION_NEXT"
        const val ACTION_STOP = "com.sachkomaxim.lab4.ACTION_STOP"
        const val PREFS_NAME = "MusicServicePrefs"
        const val PLAYLIST_KEY = "playlist"
        const val CURRENT_INDEX_KEY = "currentIndex"
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        loadPlaylist()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        initMediaSession()
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = if (isPaused) resumeMusic() else playMusic()
                override fun onPause() = pauseMusic()
                override fun onSkipToNext() = playNext()
                override fun onSkipToPrevious() = playPrevious()
                override fun onStop() = stopMusic()
            })

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_STOP
                )

            setPlaybackState(stateBuilder.build())
        }
    }

    private fun updatePlaybackState() {
        val state = when {
            isPlaying -> PlaybackStateCompat.STATE_PLAYING
            isPaused -> PlaybackStateCompat.STATE_PAUSED
            else -> PlaybackStateCompat.STATE_STOPPED
        }

        stateBuilder.setState(state, mediaPlayer?.currentPosition?.toLong() ?: 0, 1.0f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun updateMediaMetadata() {
        val currentSong = getCurrentSong() ?: return

        val albumArt = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Unknown Artist")
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Unknown Album")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer?.duration?.toLong() ?: -1)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)

        mediaSession.setMetadata(metadataBuilder.build())
    }

    private fun requestAudioFocus(): Boolean {
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
            .also { audioFocusRequest = it }

        return audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseMusic()
            AudioManager.AUDIOFOCUS_GAIN -> if (isPaused) resumeMusic()
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> if (isPaused) resumeMusic() else playMusic()
            ACTION_PAUSE -> pauseMusic()
            ACTION_PREVIOUS -> playPrevious()
            ACTION_NEXT -> playNext()
            ACTION_STOP -> stopMusic()
            Intent.ACTION_MEDIA_BUTTON -> mediaSession.controller.transportControls.play()
        }
        return START_STICKY
    }

    private fun savePlaylist() {
        val gson = Gson()
        val json = gson.toJson(playlist)
        sharedPreferences.edit {
            putString(PLAYLIST_KEY, json)
                .putInt(CURRENT_INDEX_KEY, currentSongIndex)
        }
    }

    private fun loadPlaylist() {
        val gson = Gson()
        val json = sharedPreferences.getString(PLAYLIST_KEY, null)
        if (json != null) {
            val type = object : TypeToken<List<Song>>() {}.type
            playlist = gson.fromJson(json, type)
            currentSongIndex = sharedPreferences.getInt(CURRENT_INDEX_KEY, 0)
        }
    }

    fun addToPlaylist(song: Song) {
        playlist.add(song)
        savePlaylist()
    }

    fun getPlaylist(): List<Song> = playlist

    fun getCurrentSong(): Song? =
        if (playlist.isNotEmpty() && currentSongIndex < playlist.size) playlist[currentSongIndex] else null

    fun playMusic() {
        if (playlist.isEmpty()) {
            Toast.makeText(this, "Playlist is empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (!requestAudioFocus()) {
            Toast.makeText(this, "Cannot get audio focus", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            prepareMediaPlayer()

            mediaPlayer?.start()
            isPlaying = true
            isPaused = false

            mediaSession.isActive = true
            updatePlaybackState()
            updateMediaMetadata()

            startForeground(NOTIFICATION_ID, createNotification())
            savePlaylist()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error playing music: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prepareMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setOnCompletionListener { playNext() }
            }
        } else {
            mediaPlayer?.reset()
        }

        val currentSong = playlist[currentSongIndex]
        when (currentSong.storageType) {
            MainActivity.STORAGE_INTERNAL -> {
                mediaPlayer?.setDataSource(filesDir.absolutePath + "/" + currentSong.path)
            }
            MainActivity.STORAGE_EXTERNAL -> {
                mediaPlayer?.setDataSource(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + "/" + currentSong.path)
            }
            else -> {
                mediaPlayer?.setDataSource(this, currentSong.path.toUri())
            }
        }
        mediaPlayer?.prepare()
    }

    fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPaused = true
                isPlaying = false
                updatePlaybackState()
                updateNotification()
            }
        }
    }

    fun resumeMusic() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                if (!requestAudioFocus()) {
                    Toast.makeText(this, "Cannot get audio focus", Toast.LENGTH_SHORT).show()
                    return
                }

                it.start()
                isPaused = false
                isPlaying = true
                updatePlaybackState()
                updateNotification()
            }
        }
    }

    fun playNext() {
        if (playlist.isEmpty()) return
        currentSongIndex = (currentSongIndex + 1) % playlist.size
        playMusic()
    }

    fun playPrevious() {
        if (playlist.isEmpty()) return
        currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else playlist.size - 1
        playMusic()
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        isPaused = false

        abandonAudioFocus()
        mediaSession.isActive = false
        updatePlaybackState()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun isPlaying(): Boolean = isPlaying

    fun isPaused(): Boolean = isPaused

    fun getCurrentSongIndex(): Int = currentSongIndex

    fun playAt(index: Int) {
        if (index in 0 until playlist.size) {
            currentSongIndex = index
            playMusic()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for Music Service"
            setShowBadge(true)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val currentSong = getCurrentSong() ?: return createEmptyNotification()

        val notificationIntent = Intent(this, PlaylistActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, MusicService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 0, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val previousIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val previousPendingIntent = PendingIntent.getService(
            this, 1, previousIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this, 2, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 3, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val albumArt = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentSong.title)
            .setContentText("Now Playing")
            .setSmallIcon(R.drawable.round_music_note_24)
            .setLargeIcon(albumArt)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.round_skip_previous_24, "Previous", previousPendingIntent)
            .addAction(
                if (isPlaying) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(R.drawable.round_skip_next_24, "Next", nextPendingIntent)
            .addAction(R.drawable.round_stop_24, "Stop", stopPendingIntent)
            .setStyle(MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
    }

    fun removeSongFromPlaylist(position: Int) {
        if (position in 0 until playlist.size) {
            val isRemovingCurrentSong = position == currentSongIndex

            playlist.removeAt(position)

            when {
                playlist.isEmpty() -> stopMusic()
                isRemovingCurrentSong -> {
                    currentSongIndex = currentSongIndex.coerceAtMost(playlist.size - 1)
                    playMusic()
                }
                position < currentSongIndex -> currentSongIndex--
            }

            savePlaylist()
        }
    }

    private fun createEmptyNotification(): Notification {
        val notificationIntent = Intent(this, PlaylistActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("No song playing")
            .setSmallIcon(R.drawable.round_music_note_24)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        abandonAudioFocus()
        mediaSession.release()
        super.onDestroy()
    }
}
