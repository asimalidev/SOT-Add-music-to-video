package com.addmusictovideos.audiovideomixer.sk.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _actionSource = MutableLiveData<String>()
    val actionSource: LiveData<String> get() = _actionSource
    private val _selectedAudioList = MutableLiveData<List<Audio>>()
    val selectedAudioList: LiveData<List<Audio>> get() = _selectedAudioList

    fun setActionSource(source: String) {
        _actionSource.value = source
    }

    fun setSelectedAudio(list: List<Audio>) {
        _selectedAudioList.value = list
    }
}