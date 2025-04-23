package com.addmusictovideos.audiovideomixer.sk.utils

import android.net.Uri

interface OnVideoEditedEvent {
    fun getResult(uri: Uri)
    fun onError(message: String)
}