package com.addmusictovideos.audiovideomixer.sk.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Audio(
    var title: String,
    var folderName: String,
    var id: String,
    var album: String,
    var artist: String,
    var duration: Long,
    var size: String,
    var path: String,
    var artUri: Uri
) : Parcelable
