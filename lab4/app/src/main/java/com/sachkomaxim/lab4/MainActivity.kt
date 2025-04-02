package com.sachkomaxim.lab4

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sachkomaxim.lab4.viewmodel.MainViewModel
import java.io.File

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val fileTypeRadioGroup: RadioGroup by lazy { findViewById(R.id.fileTypeRG) }
    private val storageTypeRadioGroup: RadioGroup by lazy { findViewById(R.id.storageTypeRG) }
    private val editText: EditText by lazy { findViewById(R.id.editText) }
    private val openButton: Button by lazy { findViewById(R.id.openButton) }
    private val openPlaylistButton: Button by lazy { findViewById(R.id.playlistsButton) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupListeners()

        openPlaylistButton.setOnClickListener {
            val intent = Intent(this, PlaylistActivity::class.java)
            startActivity(intent)
        }

        observeViewModel()

        openButton.setOnClickListener {
            openRequestedFile()
        }
    }

    private fun setupListeners() {
        fileTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setFileTypeRadioButtonId(checkedId)
            if (checkedId != -1) {
                val fileType = findViewById<RadioButton>(checkedId).text.toString()
                viewModel.setSelectedFileType(fileType)
            }
        }

        storageTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setStorageTypeRadioButtonId(checkedId)
            if (checkedId != -1) {
                val storageType = findViewById<RadioButton>(checkedId).text.toString()
                viewModel.setSelectedStorageType(storageType)
            }
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setFilePath(s.toString())
            }
        })
    }

    private fun observeViewModel() {
        viewModel.fileTypeRadioButtonId.observe(this) { id ->
            if (id != -1 && fileTypeRadioGroup.checkedRadioButtonId != id) {
                fileTypeRadioGroup.check(id)
            }
        }

        viewModel.storageTypeRadioButtonId.observe(this) { id ->
            if (id != -1 && storageTypeRadioGroup.checkedRadioButtonId != id) {
                storageTypeRadioGroup.check(id)
            }
        }

        viewModel.filePath.observe(this) { path ->
            if (editText.text.toString() != path) {
                editText.setText(path)
            }
        }
    }

    private fun openRequestedFile() {
        try {
            val fileType: String = getFileType()
            val storageType: String = getStorageType()
            val link: String = getLink()
            if (verifyFilePresence(fileType, storageType, link)) {
                val activityToOpen = if (fileType == FILE_VIDEO) {
                    VideoPlayerActivity::class.java
                } else {
                    AudioPlayerActivity::class.java
                }
                val intent = Intent(this, activityToOpen)
                intent.putExtra("storageType", storageType)
                intent.putExtra("link", link)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Cannot find $fileType file $link in $storageType.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            return
        }
    }

    private fun getFileType(): String {
        val checkedFileTypeRBId = fileTypeRadioGroup.checkedRadioButtonId
        if (checkedFileTypeRBId == -1) {
            Toast.makeText(this, "Please select file type", Toast.LENGTH_LONG).show()
            throw IllegalStateException()
        }
        return findViewById<RadioButton>(checkedFileTypeRBId).text.toString()
    }

    private fun getStorageType(): String {
        val checkedStorageTypeRBId = storageTypeRadioGroup.checkedRadioButtonId
        if (checkedStorageTypeRBId == -1) {
            Toast.makeText(this, "Please select storage type", Toast.LENGTH_LONG).show()
            throw IllegalStateException()
        }
        return findViewById<RadioButton>(checkedStorageTypeRBId).text.toString()
    }

    private fun getLink(): String {
        val linkText = editText.text.toString()
        if (linkText.isBlank()) {
            Toast.makeText(this, "Please enter the link to file", Toast.LENGTH_LONG).show()
            throw IllegalStateException()
        }
        return linkText
    }

    private fun verifyFilePresence(fileType: String, storageType: String, link: String): Boolean {
        return if (storageType == STORAGE_INTERNAL) {
            File(filesDir.absolutePath, link).exists()
        } else if (storageType == STORAGE_EXTERNAL) {
            if (fileType == FILE_VIDEO) {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath, link).exists()
            } else {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath, link).exists()
            }
        } else {
            true
        }
    }

    companion object {
        const val STORAGE_INTERNAL = "Internal storage"
        const val STORAGE_EXTERNAL = "External storage"
        const val STORAGE_URL = "URL"
        const val FILE_VIDEO = "Video"
    }
}
