package com.addmusictovideos.audiovideomixer.sk.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.model.Audio
import com.airbnb.lottie.LottieAnimationView
import java.util.Collections
import java.util.concurrent.TimeUnit

class MergeAudioAdapter(
    private val context: Context,
    var audioList: ArrayList<Audio>,
    private val onAudioClick: (Audio, Boolean) -> Unit,
    private val onListUpdated: (List<Audio>) -> Unit
) : RecyclerView.Adapter<MergeAudioAdapter.AudioViewHolder>() {

    private var itemTouchHelper: ItemTouchHelper? = null
    private val selectedAudios = mutableSetOf<Audio>()
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1 // Track currently playing item
    private var currentSeekBar: SeekBar? = null
    private var currentLottieAnimation: LottieAnimationView? = null
    private var currentPlayPauseButton: ImageView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.audio_merge_item, parent, false)
        return AudioViewHolder(
            view
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audio = audioList[position]
        holder.audioName.text = audio.title
        holder.size.text = formatSize(audio.size.toLong())
        holder.duration.text = formatDuration(audio.duration)


        holder.drag.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                itemTouchHelper?.startDrag(holder)
            }
            false
        }

        // Update visibility of the selection indicator
        if (selectedAudios.contains(audio)) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.appbackground
                )
            ) // Add this to highlight selected items
            holder.ivSelectedItem.visibility = View.VISIBLE
        } else {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.appbackground
                )
            ) // Default background color
            holder.ivSelectedItem.visibility = View.GONE // Hide the indicator if not selected
        }

        // Play/Pause state
        if (position == currentPlayingPosition) {
            holder.ivPlayPause.setImageResource(R.drawable.ic_paus_btn)
            holder.lottieAnimation.visibility = View.VISIBLE
            holder.seekBar.visibility = View.VISIBLE
        } else {
            holder.ivPlayPause.setImageResource(R.drawable.ic_audio)
            holder.lottieAnimation.visibility = View.GONE
            holder.seekBar.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (selectedAudios.contains(audio)) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.appbackground))
                onAudioClick(audio, false)
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.appbackground))
                onAudioClick(audio, true)
            }
        }

        holder.ivMore.setOnClickListener { view ->
            val popupMenu = PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.menu_audio_item, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_remove -> {
                        removeItem(position)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // Play/Pause Click
        holder.ivPlayPause.setOnClickListener {
            handlePlayPause(holder, position, audio)
        }
    }


    override fun getItemCount(): Int = audioList.size

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val audioName: TextView = itemView.findViewById(R.id.audioName)
        val size: TextView = itemView.findViewById(R.id.size)
        val duration: TextView = itemView.findViewById(R.id.audioDuration)
        val drag: TextView = itemView.findViewById(R.id.ivposition)
        val ivSelectedItem: ImageView = itemView.findViewById(R.id.ivSeletedItem)
        val ivPlayPause: ImageView = itemView.findViewById(R.id.ivPlaypause)
        val lottieAnimation: LottieAnimationView = itemView.findViewById(R.id.lottieAnimationView)
        val seekBar: SeekBar = itemView.findViewById(R.id.progressBar)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore) // Add this line

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
    fun removeItem(position: Int) {
        if (audioList.size <= 2) {
            Toast.makeText(context, "At least two items are required to merge!", Toast.LENGTH_SHORT).show()
            return
        }

        if (position in audioList.indices) {
            audioList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, audioList.size)

            onListUpdated(audioList)
        }
    }

    private fun handlePlayPause(
        holder: AudioViewHolder,
        position: Int,
        audio: Audio
    ) {
        if (currentPlayingPosition == position) {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    holder.ivPlayPause.setImageResource(R.drawable.ic_audio)
                    holder.lottieAnimation.visibility = View.GONE
                } else {
                    it.start()
                    holder.ivPlayPause.setImageResource(R.drawable.ic_paus_btn)
                    holder.lottieAnimation.visibility = View.VISIBLE
                    updateSeekBar(holder.seekBar)
                }
            }
        } else {
            stopCurrentAudio() // Stop the previous audio

            // Initialize a new MediaPlayer instance
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(audio.path)
                    prepare()
                    start()

                    // Update references
                    currentPlayingPosition = position
                    currentSeekBar = holder.seekBar
                    currentLottieAnimation = holder.lottieAnimation
                    currentPlayPauseButton = holder.ivPlayPause

                    // Update UI
                    holder.ivPlayPause.setImageResource(R.drawable.ic_paus_btn)
                    holder.lottieAnimation.visibility = View.VISIBLE
                    holder.seekBar.visibility = View.VISIBLE

                    // Set SeekBar max duration
                    holder.seekBar.max = duration

                    updateSeekBar(holder.seekBar)

                    setOnCompletionListener {
                        stopCurrentAudio()
                    }

                    // **Add SeekBar Change Listener**
                    holder.seekBar.setOnSeekBarChangeListener(object :
                        SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                seekTo(progress) // Seek to new position
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            pause() // Pause while seeking
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            start() // Resume playing
                            holder.ivPlayPause.setImageResource(R.drawable.ic_paus_btn)
                            holder.lottieAnimation.visibility = View.VISIBLE
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateSeekBar(seekBar: SeekBar) {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        seekBar.progress = it.currentPosition
                        handler.postDelayed(this, 500) // Update every 500ms
                    }
                }
            }
        })
    }

    private fun stopCurrentAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // Reset UI of previously playing item
        currentSeekBar?.visibility = View.GONE
        currentLottieAnimation?.visibility = View.GONE
        currentPlayPauseButton?.setImageResource(R.drawable.ic_audio)

        currentPlayingPosition = -1
    }

    // Add this function to stop the audio when the fragment is paused
    fun stopAudioOnPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun setItemTouchHelper(helper: ItemTouchHelper) {
        this.itemTouchHelper = helper
    }
}