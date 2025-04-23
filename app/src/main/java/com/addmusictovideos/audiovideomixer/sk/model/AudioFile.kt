package com.addmusictovideos.audiovideomixer.sk.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioFile(
    val id: String,
    val title: String,
    val path: String,
    val duration: Long,
    val size: Long
) : Parcelable