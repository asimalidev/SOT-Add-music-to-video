package com.addmusictovideos.audiovideomixer.sk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.model.Video
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import java.util.concurrent.TimeUnit

class VideoMergeAdapter(
    private val context: Context,
    private var selectedVideosList: ArrayList<Video>,
    private val onItemRemoved: (Video) -> Unit, // Callback for removing a video
    private val onItemClicked: (Video) -> Unit, // Callback for clicking a video
    private val onVideoRemoved: (Video) -> Unit
) : RecyclerView.Adapter<VideoMergeAdapter.MergeVideoViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()

    class MergeVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoImg: ShapeableImageView = itemView.findViewById(R.id.videoImg)
        val videoName: TextView = itemView.findViewById(R.id.videoName)
        val remove: ImageView = itemView.findViewById(R.id.ivRemoveItem)
        val duration: TextView = itemView.findViewById(R.id.duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MergeVideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.video_item_selected, parent, false)
        return MergeVideoViewHolder(view)
    }
    private var selectedPosition: Int = 0


    override fun onBindViewHolder(holder: MergeVideoViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val video = selectedVideosList[position]
        holder.videoName.text = video.title
        Glide.with(context).load(video.artUri).placeholder(R.drawable.ic_video_image)
            .into(holder.videoImg)

        holder.duration.text = formatDuration(video.duration)

        // Play video when clicking on video thumbnail
        holder.videoImg.setOnClickListener {
            onItemClicked(video)  // Trigger the callback to play the clicked video

            // Update selection
            val previousPosition = selectedPosition
            selectedPosition = position

            // Notify changes for the previous and current selected positions
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }

        // Remove video when clicking on remove button
        holder.remove.setOnClickListener {
            onVideoRemoved(video)
            removeVideo(video)
        }

        // Set background color based on whether the item is selected
        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.select_merge_item_background) // Selected background
        } else {
            holder.itemView.setBackgroundResource(R.drawable.default_merge_item_background) // Default background
        }
    }

    override fun getItemCount(): Int = selectedVideosList.size




    fun removeVideo(video: Video) {
        if (selectedVideosList.size == 1) {
            Toast.makeText(context, "At least one video must be selected.", Toast.LENGTH_SHORT).show()
            return
        }

        val position = selectedVideosList.indexOf(video)
        if (position != -1) {
            selectedVideosList.removeAt(position)
            notifyItemRemoved(position)
            onItemRemoved(video)  // Notify the fragment
        }
    }



    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}