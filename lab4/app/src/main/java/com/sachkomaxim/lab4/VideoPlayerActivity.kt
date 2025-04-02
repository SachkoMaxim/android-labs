package com.sachkomaxim.lab4

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri

class VideoPlayerActivity : AppCompatActivity() {

    private val videoView: VideoView by lazy { findViewById(R.id.videoView) }
    private var currentPosition = 0
    private var storageType: String? = null
    private var link: String? = null
    private var isVideoPlaying = false

    private val storageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        storageType = intent.getStringExtra("storageType")
        link = intent.getStringExtra("link")

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt("currentPosition", 0)
            isVideoPlaying = savedInstanceState.getBoolean("isVideoPlaying", false)
        }

        setupVideoView()
    }

    private fun setupVideoView() {
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        try {
            when (storageType) {
                MainActivity.STORAGE_INTERNAL -> {
                    videoView.setVideoPath(filesDir.absolutePath + "/" + link)
                }
                MainActivity.STORAGE_EXTERNAL -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!Environment.isExternalStorageManager()) {
                            requestPermission()
                        }
                    }
                    videoView.setVideoPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/" + link)
                }
                else -> {
                    videoView.setVideoURI(link!!.toUri())
                }
            }

            videoView.setOnPreparedListener { mp ->
                if (currentPosition > 0) {
                    videoView.seekTo(currentPosition)
                }
                if (isVideoPlaying) {
                    videoView.start()
                }
            }

            videoView.setOnCompletionListener {
                currentPosition = 0
                isVideoPlaying = false
            }

        } catch (e: Exception) {
            e.printStackTrace()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentPosition", videoView.currentPosition)
        outState.putBoolean("isVideoPlaying", videoView.isPlaying)
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            currentPosition = videoView.currentPosition
            isVideoPlaying = true
            videoView.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isVideoPlaying && currentPosition > 0) {
            videoView.seekTo(currentPosition)
            videoView.start()
        }
    }
}
