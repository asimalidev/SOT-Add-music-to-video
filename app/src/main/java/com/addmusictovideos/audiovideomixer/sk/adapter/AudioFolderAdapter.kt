package com.addmusictovideos.audiovideomixer.sk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.model.Folder

class AudioFolderAdapter(
    private val context: Context,
    private val folderList: ArrayList<Folder>,
    private val folderMediaCount: Map<String, Int>, // Can be for both video & audio
    private val isAudioFolder: Boolean, // To distinguish folder type
    private val onFolderClick: (String, Boolean) -> Unit // Pass folder name & type
) : RecyclerView.Adapter<AudioFolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderName: TextView = itemView.findViewById(R.id.folderNameFV)
        val mediaCount: TextView = itemView.findViewById(R.id.totalVideos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.folder_item, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folderList[position]
        holder.folderName.text = folder.folderName

        // Get the correct count (either videos or audios)
        val count = folderMediaCount[folder.folderName] ?: 0
        holder.mediaCount.text = "$count ${if (isAudioFolder) "audios" else "videos"}"

        holder.itemView.setOnClickListener {
            onFolderClick(folder.folderName, isAudioFolder) // Pass folder name & type
        }
    }

    override fun getItemCount(): Int = folderList.size
}