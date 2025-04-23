package com.addmusictovideos.audiovideomixer.sk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.model.Audio
import com.addmusictovideos.audiovideomixer.sk.model.SharedViewModel
import java.util.concurrent.TimeUnit

class SelectedAudioAdapter(
    private val context: Context,
    private var selectedVideosList: ArrayList<Audio>,
    private val onItemRemoved: (Audio) -> Unit,
    private var navController: NavController,
    private var sharedViewModel: SharedViewModel,

//    private val onNavigateToAudioSpeed: (Audio) -> Unit,
//    private val onNavigateToSplitAudio: (Audio) -> Unit,
//    private val onNavigateToconvertaudio: (Audio) -> Unit,
    private val onNavigateToChangeMusic: (Audio) -> Unit,
    private val onNavigateToCutVideo: (Audio) -> Unit
) : RecyclerView.Adapter<SelectedAudioAdapter.SelectedVideosViewHolder>() {


    init {
        this.navController = navController
        this.sharedViewModel = sharedViewModel
    }

    class SelectedVideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoName: TextView = itemView.findViewById(R.id.audioName)
        val duration: TextView = itemView.findViewById(R.id.audioDuration) // Change to TextView
        val remove: ImageView = itemView.findViewById(R.id.ivRemoveItem)
        val size: TextView = itemView.findViewById(R.id.size) // Change to TextView
        val tvItemNumber: TextView = itemView.findViewById(R.id.ivSeletedItem) // New TextView

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedVideosViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.selected_audio_item, parent, false)
        return SelectedVideosViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedVideosViewHolder, position: Int) {
        val audio = selectedVideosList[position]

        holder.videoName.text = audio.title
        holder.size.text = formatSize(audio.size.toLong())
        holder.duration.text = formatDuration(audio.duration)

        // Set item number (1-based index)
        holder.tvItemNumber.text = (position + 1).toString()

        holder.remove.setOnClickListener {
            removeVideo(audio)
        }
    }



    private fun formatSize(size: Long): String {
        val kb = size / 1024
        val mb = kb / 1024
        val gb = mb / 1024

        return when {
            gb >= 1 -> "${gb}GB"
            mb >= 1 -> "${mb}MB"
            kb >= 1 -> "${kb}KB"
            else -> "$size B"
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }



    override fun getItemCount(): Int = selectedVideosList.size



    fun addVideo(video: Audio) {
        when (sharedViewModel.actionSource.value) {
            // **Single selection mode** - Always keep only 1 item
            "cutaudio", "compressvideo", "VideotoGif", "ReverseVideo", "VideoSpeed", "ConvertVideo","audiospeed","splitaudio","changemusic" -> {
                selectedVideosList.clear()
                selectedVideosList.add(video)
                notifyDataSetChanged()
            }

            // **Two-selection mode** - Allow exactly 2 items, remove the oldest if a third is added
            "mixaudio" -> {
                if (selectedVideosList.size >= 2) {
                    selectedVideosList.removeAt(0) // Keep the last 2 selected items
                    notifyDataSetChanged()
                }
                selectedVideosList.add(video)
                notifyItemInserted(selectedVideosList.size - 1)
            }

            // **Two-selection mode** - Allow exactly 2 items, remove the oldest if a third is added
            "audioconvert" -> {
                if (selectedVideosList.size >= 2) {
                    selectedVideosList.removeAt(0) // Keep the last 2 selected items
                    notifyDataSetChanged()
                }
                selectedVideosList.add(video)
                notifyItemInserted(selectedVideosList.size - 1)
            }

            // **Multiple selection mode** - Allow unlimited selections
            "mergeaudio" -> {
                selectedVideosList.add(video)
                notifyItemInserted(selectedVideosList.size - 1)
            }
        }

//        // If navigating is required for specific cases
        if (sharedViewModel.actionSource.value == "cutaudio") {
            onNavigateToCutVideo(video)
        }

        if (sharedViewModel.actionSource.value == "changemusic") {
            onNavigateToChangeMusic(video)
        }

//        if (sharedViewModel.actionSource.value == "audiospeed") {
//            onNavigateToAudioSpeed(video)
//        }

//        if (sharedViewModel.actionSource.value == "splitaudio") {
//            onNavigateToSplitAudio(video)
//
//        }
    }


    fun removeVideo(video: Audio) {
        selectedVideosList.remove(video)
        notifyDataSetChanged()
        onItemRemoved(video) // Notify VideoAdapter that the item is removed
    }


    fun removeAllVideos() {
        val removedVideos = ArrayList(selectedVideosList) // Copy list before clearing
        selectedVideosList.clear()
        notifyDataSetChanged()
        // Notify VideoAdapter to unselect all
        removedVideos.forEach { onItemRemoved(it) }
    }


    fun getSelectedVideos(): ArrayList<Audio> {
        return selectedVideosList
    }

    fun getSelectedCount(): Int {
        return selectedVideosList.size
    }
}