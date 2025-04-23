package com.addmusictovideos.audiovideomixer.sk.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentPlayVideo2Binding

class PlayVideoFragment : Fragment() {
    private lateinit var videoPath: String
    private var exoPlayer: ExoPlayer? = null
    private lateinit var binding: FragmentPlayVideo2Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayVideo2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoPath = PlayVideoFragmentArgs.fromBundle(requireArguments()).path
        initializePlayer(videoPath)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action =
                        PlayVideoFragmentDirections.actionOutputplayerToOuputfolder("video")
                    findNavController().navigate(action)
                }
            })
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(videoPath: String) {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(5000, 10000, 1500, 2000) // Reduce buffer size to optimize memory
            .build()

        exoPlayer = ExoPlayer.Builder(requireContext()).setLoadControl(loadControl).build().apply {
                val mediaItem = MediaItem.fromUri(videoPath)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }

        binding.playerView.player = exoPlayer
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.playWhenReady = true // Resume playback without reinitializing
        toggleUIVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause() // Pause instead of releasing to prevent reloading
        toggleUIVisibility(View.VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    private fun releasePlayer() {
        exoPlayer?.apply {
            stop()
            clearMediaItems() // Clear media items before releasing
            release()
        }
        exoPlayer = null
    }

    private fun toggleUIVisibility(visibility: Int) {
        requireActivity().findViewById<ConstraintLayout>(R.id.clToolbar).visibility = visibility
    }
}