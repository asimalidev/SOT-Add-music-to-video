package com.addmusictovideos.audiovideomixer.sk.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.ItemFileLayoutBinding
import com.addmusictovideos.audiovideomixer.sk.fragments.PlayOutputAudioFragmentArgs
import com.addmusictovideos.audiovideomixer.sk.fragments.PlayVideoFragmentArgs
import com.addmusictovideos.audiovideomixer.sk.utils.PlaceholderContent
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.Locale

class NewFileListAdapter(
    private val values: ArrayList<PlaceholderContent.PlaceholderItem>,
    private val navController: NavController
) : RecyclerView.Adapter<NewFileListAdapter.ViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemFileLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(values[position])
    }

    override fun getItemCount(): Int = values.size

    fun getFolderSizeLabel(path: String): String {
        val formatter = DecimalFormat("##.##", DecimalFormatSymbols(Locale.US))
        var size = getFolderSize(path).toDouble() / 1000.0
        return if (size >= 1024) {
            size = formatter.format(size / 1024).toDouble()
            "$size MB"
        } else {
            size = formatter.format(size).toDouble()
            "$size KB"
        }
    }

    fun getFolderSize(path: String): Long {
        val file = File(path)
        return if (file.isDirectory) {
            file.listFiles()?.sumOf { getFolderSize(it.absolutePath) } ?: 0
        } else {
            file.length()
        }
    }

    fun getFileTime(url: String): String {
        val file = File(url)
        val lastModDate = Date(file.lastModified())
        return lastModDate.toString()
    }

    inner class ViewHolder(private val binding: ItemFileLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val iconThumb = binding.songThumb
        private val icMenuItem = binding.btnMenuItem
        private val name = binding.nameTxt
        private val timeTxt = binding.timeTxt
        private val sizeTxt = binding.sizeTxt
        private val songTimeTxt = binding.songTimeTxt


        fun bind(item: PlaceholderContent.PlaceholderItem) {
            val videoExtensions = setOf("mp4", "avi", "mkv", "mov", "webm", "flv", "wmv", "mpeg", "mpg")
            val audioExtensions = setOf("mp3", "ogg", "flac", "m4a", "wav", "aac", "wma", "aiff", "alac")

            val fileNameLower = item.name.lowercase(Locale.getDefault())
            val fileExtension = fileNameLower.substringAfterLast(".", "")

            val isVideo = fileExtension in videoExtensions
            val isAudio = fileExtension in audioExtensions
            val isGif = fileExtension == "gif" || fileNameLower.endsWith(".gif")

            when {
                isGif -> {
                    songTimeTxt.visibility = View.GONE
                    Glide.with(context)
                        .asGif()
                        .load(item.url)
                        .into(iconThumb)
                }

                isAudio -> {
                    songTimeTxt.visibility = View.GONE
                    Glide.with(context)
                        .asBitmap()
                        .load(R.drawable.ic_music)
                        .into(iconThumb)
                }

                isVideo -> {
                    songTimeTxt.visibility = View.GONE
                    // Use coroutine for heavy thumbnail loading
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = getVideoThumbnail(item.url)
                        withContext(Dispatchers.Main) {
                            if (bitmap != null) {
                                Glide.with(context)
                                    .asBitmap()
                                    .load(bitmap)
                                    .into(iconThumb)
                            } else {
                                Glide.with(context)
                                    .asBitmap()
                                    .load(R.drawable.ic_video_image)
                                    .into(iconThumb)
                            }
                        }
                    }
                }

                else -> {
                    songTimeTxt.visibility = View.GONE
                    Glide.with(context)
                        .asBitmap()
                        .load(R.drawable.ic_video_image)
                        .into(iconThumb)
                }
            }

            name.text = item.name
            sizeTxt.text = getFolderSizeLabel(item.url)
            timeTxt.text = getFileTime(item.url)

            itemView.setOnClickListener {
                when {
                    isVideo -> {
                        val action = PlayVideoFragmentArgs.Builder("video", item.url).build()
                        navController.navigate(R.id.action_outputfolder_to_video_play, action.toBundle())
                    }

                    isAudio -> {
                        val action = PlayOutputAudioFragmentArgs.Builder("audio", item.url).build()
                        navController.navigate(R.id.action_outputfolder_to_audio_play, action.toBundle())
                    }

                    else -> {
                        // Optional fallback
                    }
                }
            }

            icMenuItem.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && pos < values.size) {
                    showMenu(it, values[pos].url, pos)
                }
            }
        }


        private fun getVideoThumbnail(videoPath: String): Bitmap? {
            return try {
                val retriever = MediaMetadataRetriever().apply {
                    setDataSource(videoPath)
                }
                val bitmap = retriever.frameAtTime
                retriever.release()
                bitmap
            } catch (e: Exception) {
                null
            }
        }

        @SuppressLint("MissingInflatedId")
        private fun showMenu(anchor: View, path: String, adapterPosition: Int) {
            val popupView = LayoutInflater.from(context).inflate(R.layout.menu_layout, null)
            val popupWindow = PopupWindow(popupView, WRAP_CONTENT, WRAP_CONTENT, true).apply {
                isOutsideTouchable = true
                isFocusable = true
                elevation = 10f
            }

            popupView.findViewById<TextView>(R.id.menu_share).setOnClickListener {
                shareFile()
                popupWindow.dismiss()
            }

            popupView.findViewById<TextView>(R.id.menu_info).setOnClickListener {
                showFileInfo()
                popupWindow.dismiss()
            }

            popupView.findViewById<TextView>(R.id.menu_delete).setOnClickListener {
                deleteFile()
                popupWindow.dismiss()
            }

            popupWindow.showAsDropDown(anchor)
        }

        private fun shareFile() {
            try {
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    File(values[adapterPosition].url)
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Check out this file")
                    putExtra(Intent.EXTRA_TEXT, "Sharing from my app!")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                context.startActivity(Intent.createChooser(intent, "Share File"))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to share file", Toast.LENGTH_SHORT).show()
            }
        }

        private fun showFileInfo() {
            AlertDialog.Builder(context)
                .setTitle("File Info")
                .setMessage("Path: ${values[adapterPosition].url}")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        private fun deleteFile() {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.deleting_dialog, null)
            val alertDialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                alertDialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
                File(values[adapterPosition].url).delete()
                values.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                Toast.makeText(context, "File Deleted Successfully!", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }

            alertDialog.show()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        }
    }
}