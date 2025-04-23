package com.addmusictovideos.audiovideomixer.sk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.model.AudioFile


class ExtractAudioAdapter(
    private val context: Context,
    audioFiles: List<AudioFile>,
    private val onItemClicked: (AudioFile) -> Unit,
    private val onMoreClicked: (AudioFile) -> Unit
) : RecyclerView.Adapter<ExtractAudioAdapter.AudioViewHolder>() {

    private val _audioFiles = audioFiles.toMutableList()
    private val selectedItems = mutableSetOf<Int>().apply {
        addAll(_audioFiles.indices)
    }

    // Public read-only access to audio files
    val audioFiles: List<AudioFile> get() = _audioFiles

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val audioName: TextView = itemView.findViewById(R.id.audioName)
        val audioDuration: TextView = itemView.findViewById(R.id.audioDuration)
        val size: TextView = itemView.findViewById(R.id.size)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
        val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)
        val ivSeletedItem: ImageView = itemView.findViewById(R.id.ivSeletedItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.merge_item_audio, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioFile = _audioFiles[position]

        holder.audioName.text = audioFile.title
        holder.audioDuration.text = formatDuration(audioFile.duration)
        holder.size.text = formatSize(audioFile.size)

        // Set selection state
        val isSelected = selectedItems.contains(position)
        updateSelectionUI(holder, isSelected)

        holder.itemView.setOnClickListener {
            toggleSelection(position)
            onItemClicked(audioFile)
        }

        holder.ivMore.setOnClickListener { onMoreClicked(audioFile) }
    }

    override fun getItemCount() = _audioFiles.size

    // Public functions
    fun addAudioFile(audioFile: AudioFile) {
        _audioFiles.add(audioFile)
        selectedItems.add(_audioFiles.size - 1) // Select new item by default
        notifyItemInserted(_audioFiles.size - 1)
    }

    fun getSelectedItems(): List<AudioFile> {
        return selectedItems.map { position -> _audioFiles[position] }
    }

    fun selectAll() {
        selectedItems.addAll(_audioFiles.indices)
        notifyDataSetChanged()
    }

    fun clearSelections() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    // Private helper functions
    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position)
    }

    private fun updateSelectionUI(holder: AudioViewHolder, isSelected: Boolean) {
        holder.ivSelected.setImageResource(
            if (isSelected) R.drawable.ic_audio_selected
            else R.drawable.ic_unselect_audio
        )

        holder.ivSeletedItem.setImageResource(
            if (isSelected) R.drawable.ic_done_audio
            else R.drawable.ic_unslect_item
        )
    }

    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun formatSize(sizeBytes: Long): String {
        return when {
            sizeBytes >= 1024 * 1024 -> "%.2f MB".format(sizeBytes / (1024.0 * 1024.0))
            sizeBytes >= 1024 -> "%.2f KB".format(sizeBytes / 1024.0)
            else -> "$sizeBytes B"
        }
    }
}