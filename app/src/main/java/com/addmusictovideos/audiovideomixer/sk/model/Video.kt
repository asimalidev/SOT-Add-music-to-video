package com.addmusictovideos.audiovideomixer.sk.model

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import android.os.Parcel
import android.os.Parcelable
import com.addmusictovideos.audiovideomixer.sk.activities.MainActivity

data class Video(
    val id: String,
    var title: String,
    val duration: Long = 0,
    val folderName: String,
    val size: String,
    var path: String,
    var artUri: Uri
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeLong(duration)
        parcel.writeString(folderName)
        parcel.writeString(size)
        parcel.writeString(path)
        parcel.writeParcelable(artUri, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Video> {
        override fun createFromParcel(parcel: Parcel): Video {
            return Video(parcel)
        }

        override fun newArray(size: Int): Array<Video?> {
            return arrayOfNulls(size)
        }
    }
}


data class Folder(val id: String, val folderName: String)



@SuppressLint("InlinedApi", "Recycle", "Range")
fun getAllVideos(context: Context): ArrayList<Video> {
    val sortEditor = context.getSharedPreferences("Sorting", AppCompatActivity.MODE_PRIVATE)
    MainActivity.sortValue = sortEditor.getInt("sortValue", 0)

    // For avoiding duplicate folders
    MainActivity.folderList = ArrayList()

    val tempList = ArrayList<Video>()
    val tempFolderList = ArrayList<String>()
    val projection = arrayOf(
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.BUCKET_ID
    )
    val cursor = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
        MainActivity.sortList[MainActivity.sortValue]
    )
    if (cursor != null)
        if (cursor.moveToNext())
            do {
                val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)) ?: "Unknown"
                val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID)) ?: "Unknown"
                val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)) ?: "Internal Storage"
                val folderIdC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)) ?: "Unknown"
                val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)) ?: "0"
                val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)) ?: "Unknown"
                val durationC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))?.toLong() ?: 0L

                // **Skip video if its duration is 0**
                if (durationC <= 0) continue

                try {
                    val file = File(pathC)
                    val artUriC = Uri.fromFile(file)
                    val video = Video(
                        title = titleC,
                        id = idC,
                        folderName = folderC,
                        duration = durationC,
                        size = sizeC,
                        path = pathC,
                        artUri = artUriC
                    )
                    if (file.exists()) tempList.add(video)

                    // For adding folders
                    if (!tempFolderList.contains(folderC) && !folderC.contains("Internal Storage")) {
                        tempFolderList.add(folderC)
                        MainActivity.folderList.add(Folder(id = folderIdC, folderName = folderC))
                    }
                } catch (e: Exception) {
                }
            } while (cursor.moveToNext())
    cursor?.close()
    return tempList
}

