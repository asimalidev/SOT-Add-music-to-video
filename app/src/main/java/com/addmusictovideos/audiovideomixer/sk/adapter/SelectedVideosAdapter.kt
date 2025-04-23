package com.addmusictovideos.audiovideomixer.sk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.model.SharedViewModel
import com.addmusictovideos.audiovideomixer.sk.model.Video
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView


class SelectedVideosAdapter(
    private val context: Context,
    private var selectedVideosList: ArrayList<Video>,
    private val onItemRemoved: (Video) -> Unit,
    private var navController: NavController,
    private var sharedViewModel: SharedViewModel,
    private val onNavigateTochangemusic: (Video) -> Unit,
    private val onNavigateToExtractAudio: (Video) -> Unit,
    private val onNavigateToFastVideo: (Video) -> Unit,
    private val onNavigateToSlowVideo: (Video) -> Unit,
    private val onNavigateToCutVideo: (Video) -> Unit
//    private val onNavigateTocompressvideo: (Video) -> Unit,
//    private val onNavigateTovideotogif: (Video) -> Unit,
//    private val onNavigateToreversevideo: (Video) -> Unit,
//    private val onNavigateTospeedvideo: (Video) -> Unit,
//    private val onNavigateToconvertvideo: (Video) -> Unit,
//    private val onNavigateTochangemusic: (Video) -> Unit,
//    private val onNavigateTofiltervideo: (Video) -> Unit
) : RecyclerView.Adapter<SelectedVideosAdapter.SelectedVideosViewHolder>() {


    init {
        this.navController = navController
        this.sharedViewModel = sharedViewModel
    }

    class SelectedVideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoImg: ShapeableImageView = itemView.findViewById(R.id.videoImg)
        val videoName: TextView = itemView.findViewById(R.id.videoName)
        val remove: ImageView = itemView.findViewById(R.id.ivRemoveItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedVideosViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.video_item_selected, parent, false)
        return SelectedVideosViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedVideosViewHolder, position: Int) {
        val video = selectedVideosList[position]
        holder.videoName.text = video.title
        Glide.with(context).load(video.artUri).placeholder(R.drawable.ic_video_image)
            .into(holder.videoImg)

        // Remove video when clicking on videoImg
        holder.remove.setOnClickListener {
            removeVideo(video)
        }
    }

    override fun getItemCount(): Int = selectedVideosList.size


    fun addVideo(video: Video) {
        if (sharedViewModel.actionSource.value == "trimvideo"
            || sharedViewModel.actionSource.value == "extractaudio"
            || sharedViewModel.actionSource.value == "fastvideo"
            || sharedViewModel.actionSource.value == "slowvideo"
            || sharedViewModel.actionSource.value == "compressvideo"
            || sharedViewModel.actionSource.value == "VideotoGif"
            || sharedViewModel.actionSource.value == "ReverseVideo"
            || sharedViewModel.actionSource.value == "VideoSpeed"
            || sharedViewModel.actionSource.value == "ConvertVideo"
            || sharedViewModel.actionSource.value == "changemusic"
            || sharedViewModel.actionSource.value == "FilterVideo"
        ) {
            // Ensure only one video is selected
            selectedVideosList.clear()
            notifyDataSetChanged()
        }

        selectedVideosList.add(video)
        notifyItemInserted(selectedVideosList.size - 1)

//         Call the callback to navigate
        if (sharedViewModel.actionSource.value == "trimvideo") {
           onNavigateToCutVideo(video)
        } else if (sharedViewModel.actionSource.value == "extractaudio") {
            onNavigateToExtractAudio(video) // Add your corresponding function here

        }
        else if (sharedViewModel.actionSource.value == "fastvideo") {
            onNavigateToFastVideo(video) // Add your corresponding function here
        }
        else if (sharedViewModel.actionSource.value == "slowvideo") {
            onNavigateToSlowVideo(video) // Add your corresponding function here
        }

//        else if (sharedViewModel.actionSource.value == "compressvideo") {
//            onNavigateTocompressvideo(video) // Add your corresponding function here
//        } else if (sharedViewModel.actionSource.value == "VideotoGif") {
//            onNavigateTovideotogif(video) // Add your corresponding function here
//        }
//        else if (sharedViewModel.actionSource.value == "ReverseVideo") {
//            onNavigateToreversevideo(video) // Add your corresponding function here
//        }
//        else if (sharedViewModel.actionSource.value == "VideoSpeed") {
//            onNavigateTospeedvideo(video) // Add your corresponding function here
//        }
//
//        else if (sharedViewModel.actionSource.value == "ConvertVideo") {
//            onNavigateToconvertvideo(video) // Add your corresponding function here
//        }

        else if (sharedViewModel.actionSource.value == "changemusic") {
            onNavigateTochangemusic(video) // Add your corresponding function here
        }

//        else if (sharedViewModel.actionSource.value == "FilterVideo") {
//            onNavigateTofiltervideo(video) // Add your corresponding function here
//        }
    }


    fun removeVideo(video: Video) {
        selectedVideosList.remove(video)
        notifyDataSetChanged()
        onItemRemoved(video) // Notify VideoAdapter that the item is removed
    }

    // Get paths of all selected videos
    fun getSelectedVideoPaths(): ArrayList<String> {
        return ArrayList(selectedVideosList.map { it.path })
    }

    fun removeAllVideos() {
        val removedVideos = ArrayList(selectedVideosList) // Copy list before clearing
        selectedVideosList.clear()
        notifyDataSetChanged()
        // Notify VideoAdapter to unselect all
        removedVideos.forEach { onItemRemoved(it) }
    }


    fun getSelectedVideos(): ArrayList<Video> {
        return selectedVideosList
    }

    fun getSelectedCount(): Int {
        return selectedVideosList.size
    }
}

