package com.addmusictovideos.audiovideomixer.sk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.model.Video
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import java.util.concurrent.TimeUnit

class VideoAdapter(
    private val context: Context,
    private var videoList: ArrayList<Video>,
    private val onVideoClick: (Video, Boolean) -> Unit // Callback for selection state
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private val selectedVideos = mutableSetOf<Video>() // Track selected videos

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoImg: ShapeableImageView = itemView.findViewById(R.id.videoImg)
        val videoName: TextView = itemView.findViewById(R.id.videoName)
        val duration: TextView = itemView.findViewById(R.id.duration)
        val ivSeletedItem: ImageView = itemView.findViewById(R.id.ivSeletedItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        holder.videoName.text = video.title
        holder.duration.text = formatDuration(video.duration)

        Glide.with(context)
            .load(video.artUri)
            .placeholder(R.drawable.ic_video_image)
            .into(holder.videoImg)

        // Update UI based on selection state
        if (selectedVideos.contains(video)) {
            holder.ivSeletedItem.setImageResource(R.drawable.ic_selected_video) // Selected state
        } else {
            holder.ivSeletedItem.setImageResource(R.drawable.ic_unslect_item) // Unselected state
        }

        holder.itemView.setOnClickListener {
            if (selectedVideos.contains(video)) {
                selectedVideos.remove(video)
                holder.ivSeletedItem.setImageResource(R.drawable.ic_unslect_item) // Unselect item
                onVideoClick(video, false) // Notify removal
            } else {
                selectedVideos.add(video)
                holder.ivSeletedItem.setImageResource(R.drawable.ic_selected_video) // Select item
                onVideoClick(video, true) // Notify addition
            }
        }
    }

    override fun getItemCount(): Int = videoList.size

    fun deselectVideo(video: Video) {
        if (selectedVideos.contains(video)) {
            selectedVideos.remove(video)
            notifyDataSetChanged()
        }
    }

    fun loadAllVideos(newList: ArrayList<Video>, selectedVideos: ArrayList<Video>) {
        // Update the video list
        videoList = newList
        // Retain the selected videos
        this.selectedVideos.clear()
        this.selectedVideos.addAll(selectedVideos)

        // Refresh the adapter
        notifyDataSetChanged()
    }


    fun getSelectedVideos(): Set<Video> {
        return selectedVideos
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: ArrayList<Video>) {
        videoList = newList
        selectedVideos.clear() // Reset selection when updating list
        notifyDataSetChanged()
    }



    fun updateData(newVideos: ArrayList<Video>) {
        videoList.clear()
        videoList.addAll(newVideos)
        notifyDataSetChanged()
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}




