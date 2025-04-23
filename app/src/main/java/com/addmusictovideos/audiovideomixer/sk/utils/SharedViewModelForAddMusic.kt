package com.addmusictovideos.audiovideomixer.sk.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModelForAddMusic : ViewModel() {
    private val _videoPath = MutableLiveData<String>()
    val videoPath: LiveData<String> get() = _videoPath

    private val _audioPath = MutableLiveData<String>()
    val audioPath: LiveData<String> get() = _audioPath

    fun setVideoPath(path: String) {
        _videoPath.value = path
    }

    fun setAudioPath(path: String) {
        _audioPath.value = path
    }
}