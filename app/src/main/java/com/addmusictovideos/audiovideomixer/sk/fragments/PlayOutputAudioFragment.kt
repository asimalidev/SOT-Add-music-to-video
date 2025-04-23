package com.addmusictovideos.audiovideomixer.sk.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentPlayOutputAudioBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.util.concurrent.TimeUnit

class PlayOutputAudioFragment : Fragment() {
    private lateinit var audioPath: String
    private var exoPlayer: ExoPlayer? = null
    private lateinit var binding: FragmentPlayOutputAudioBinding
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayOutputAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = PlayOutputAudioFragmentArgs.fromBundle(requireArguments())
        audioPath = args.path

        initializePlayer(audioPath)

        // Set file name as title
        val file = File(audioPath)
        binding.audioTitle.text = file.nameWithoutExtension

        // Play/Pause Button
        binding.btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        // SeekBar Change Listener
        binding.audioSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoPlayer?.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Rewind & Forward
        binding.btnRewind.setOnClickListener {
            exoPlayer?.seekTo((exoPlayer?.currentPosition ?: 0) - 5000) // 5 seconds back
        }
        binding.btnForward.setOnClickListener {
            exoPlayer?.seekTo((exoPlayer?.currentPosition ?: 0) + 5000) // 5 seconds forward
        }

        // Handle Back Press
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    releasePlayer() // Ensure audio stops before navigating
                    val action = PlayOutputAudioFragmentDirections.actionNavPlayAudioToFinaloutput(
                        audioPath,
                        audioPath
                    )
                    findNavController().navigate(action)
                }
            })

        binding.ivBack.setOnClickListener {
            releasePlayer() // Ensure audio stops before navigating
            val action = PlayOutputAudioFragmentDirections.actionNavPlayAudioToFinaloutput(audioPath, audioPath)
            findNavController().navigate(action)
        }
    }

    private fun initializePlayer(audioPath: String) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(requireContext()).build()
        }

        val mediaItem = MediaItem.fromUri(audioPath)
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause)

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {

                        Player.STATE_READY -> {
                            binding.audioSeekbar.max = duration.toInt()
                            binding.audioTotalDuration.text = formatTime(duration)
                            updateSeekBar()
                        }

                        Player.STATE_ENDED -> {
                            exoPlayer?.playWhenReady = false  // Add this to stop playback
                            binding.btnPlayPause.setImageResource(R.drawable.ic_playpause)
                            binding.audioSeekbar.progress = 0
                            binding.audioCurrentTime.text = formatTime(0)
                            seekTo(0)
                            handler.removeCallbacksAndMessages(null)
                        }
                    }
                }
            })
        }
    }


    private fun togglePlayPause() {
        exoPlayer?.let { player ->
            val wasPlaying = player.isPlaying
            player.playWhenReady = !wasPlaying
            updatePlayPauseButton()

            if (!wasPlaying) {
                updateSeekBar() // Resume updating the seek bar if playback resumes
            }
        }
    }



    private fun updatePlayPauseButton() {
        val isPlaying = exoPlayer?.isPlaying == true
        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_playpause
        binding.btnPlayPause.setImageResource(playPauseIcon)
    }

    private fun updateSeekBar() {
        handler.removeCallbacksAndMessages(null) // avoid multiple callbacks

        exoPlayer?.let { player ->
            binding.audioSeekbar.progress = player.currentPosition.toInt()
            binding.audioCurrentTime.text = formatTime(player.currentPosition)

            val isEnded = player.playbackState == Player.STATE_ENDED
            if (player.isPlaying && !isEnded) {
                handler.postDelayed({ updateSeekBar() }, 500)
            }
        }
    }



    private fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60

        // Handle negative time values (e.g., when seeking past the start of the audio)
        val formattedMinutes = if (minutes < 0) 0 else minutes
        val formattedSeconds = if (seconds < 0) 0 else seconds

        return String.format("%02d:%02d", formattedMinutes, formattedSeconds)
    }


    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onResume() {
        super.onResume()
        toggleUIVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        releasePlayer() // Stop playback when fragment is paused
        toggleUIVisibility(View.VISIBLE)
    }

    private fun toggleUIVisibility(visibility: Int) {
        requireActivity().findViewById<ConstraintLayout>(R.id.clToolbar).visibility = visibility
    }
}