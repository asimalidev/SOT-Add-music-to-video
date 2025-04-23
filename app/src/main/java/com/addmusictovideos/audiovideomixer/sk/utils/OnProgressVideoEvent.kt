package com.addmusictovideos.audiovideomixer.sk.utils

interface OnProgressVideoEvent {
    fun updateProgress(time: Float, max: Long, scale: Long)
}