package com.sachkomaxim.lab4.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _selectedFileType = MutableLiveData<String>()
    val selectedFileType: LiveData<String> = _selectedFileType

    private val _selectedStorageType = MutableLiveData<String>()
    val selectedStorageType: LiveData<String> = _selectedStorageType

    private val _filePath = MutableLiveData<String>()
    val filePath: LiveData<String> = _filePath

    private val _fileTypeRadioButtonId = MutableLiveData<Int>()
    val fileTypeRadioButtonId: LiveData<Int> = _fileTypeRadioButtonId

    private val _storageTypeRadioButtonId = MutableLiveData<Int>()
    val storageTypeRadioButtonId: LiveData<Int> = _storageTypeRadioButtonId

    fun setSelectedFileType(fileType: String) {
        _selectedFileType.value = fileType
    }

    fun setSelectedStorageType(storageType: String) {
        _selectedStorageType.value = storageType
    }

    fun setFilePath(path: String) {
        _filePath.value = path
    }

    fun setFileTypeRadioButtonId(id: Int) {
        _fileTypeRadioButtonId.value = id
    }

    fun setStorageTypeRadioButtonId(id: Int) {
        _storageTypeRadioButtonId.value = id
    }
}
