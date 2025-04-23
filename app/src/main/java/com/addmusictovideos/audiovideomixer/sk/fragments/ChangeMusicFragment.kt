package com.addmusictovideos.audiovideomixer.sk.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentChangeMusicBinding
import com.addmusictovideos.audiovideomixer.sk.utils.SharedViewModelForAddMusic
import com.google.android.material.bottomnavigation.BottomNavigationView


class ChangeMusicFragment : Fragment() {
    private lateinit var binding: FragmentChangeMusicBinding
    private lateinit var audioPath: String
    private lateinit var videoPath: String
    private var exoPlayer: ExoPlayer? = null
    private var audioPlayer: ExoPlayer? = null
    private lateinit var sharedViewModel: SharedViewModelForAddMusic

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeMusicBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModelForAddMusic::class.java]
        val args = ChangeMusicFragmentArgs.fromBundle(requireArguments())
        audioPath = args.audioPath
        videoPath = args.videoPath

        binding.ivBack.setOnClickListener {
            showExitConfirmationDialog()
        }
        Log.e("paths", "audio path is $audioPath and video path is $videoPath")
        sharedViewModel.setVideoPath(videoPath)
        sharedViewModel.setAudioPath(audioPath)

        // Observe ViewModel changes
        sharedViewModel.videoPath.observe(viewLifecycleOwner) { updatedVideoPath ->
            Log.e("SharedViewModel", "Updated video path: $updatedVideoPath")
            initializePlayer(updatedVideoPath)
        }

        sharedViewModel.audioPath.observe(viewLifecycleOwner) { updatedAudioPath ->
            Log.e("SharedViewModel", "Updated audio path: $updatedAudioPath")
            playBackgroundAudio(updatedAudioPath)
        }

        initializePlayer(videoPath)
        playBackgroundAudio(audioPath)

        binding.tvCutAudio.setOnClickListener {
            val action = ChangeMusicFragmentDirections.actionNavChangemusicToNavCutaudio(audioPath, videoPath)
            findNavController().navigate(action)
        }

        binding.tvCutVideo.setOnClickListener {
            val action = ChangeMusicFragmentDirections.actionNavChangemusicToNavCutvideo(videoPath, audioPath)
            findNavController().navigate(action)
        }

        binding.ivSave.setOnClickListener {
            val action = ChangeMusicFragmentDirections.actionNavChangemusicToNavChangemusicprogress(audioPath,videoPath)
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        toggleUIVisibility(View.GONE)
        exoPlayer?.playWhenReady = true
        audioPlayer?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.release()
        exoPlayer = null

        audioPlayer?.release()
        audioPlayer = null

        toggleUIVisibility(View.VISIBLE)
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                // Navigate to NavAllVideo
                val action = ChangeMusicFragmentDirections.actionNavChangeMusicToNavHome(


                )
                findNavController().navigate(action)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun initializePlayer(videoPath: String) {
        exoPlayer?.release() // Release old player if exists
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri(videoPath)
        exoPlayer?.setMediaItem(mediaItem)

        // ✅ Mute the video
        exoPlayer?.volume = 0f

        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true

        exoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    audioPlayer?.playWhenReady = true
                } else {
                    audioPlayer?.playWhenReady = false
                }
            }
        })
    }

    private fun playBackgroundAudio(audioPath: String) {
        audioPlayer?.release() // Release previous instance if exists
        audioPlayer = ExoPlayer.Builder(requireContext()).build()

        val audioItem = MediaItem.fromUri(audioPath)
        audioPlayer?.setMediaItem(audioItem)

        audioPlayer?.prepare()
        audioPlayer?.playWhenReady = true
    }

    private fun toggleUIVisibility(visibility: Int) {
        requireActivity().findViewById<ConstraintLayout>(R.id.clToolbar).visibility = visibility
    }
}