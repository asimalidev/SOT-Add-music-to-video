package com.addmusictovideos.audiovideomixer.sk.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentPlayVideoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class PlayOutputVideoFragment : Fragment() {
    private lateinit var binding: FragmentPlayVideoBinding
    private var exoPlayer: ExoPlayer? = null
    private lateinit var videoPath: String
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayVideoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = PlayOutputVideoFragmentDirections.actionFinaloutputToNavConvertvideoConversion(videoPath)
                    findNavController().navigate(action)

                }
            }
        )

        binding.ivBack.setOnClickListener {
            val action = PlayOutputVideoFragmentDirections.actionFinaloutputToNavConvertvideoConversion(videoPath)
            findNavController().navigate(action)
        }
    }

    private fun initializePlayer(videoPath: String) {
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = exoPlayer
        val mediaItem = MediaItem.fromUri(videoPath)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onResume() {
        super.onResume()
        videoPath = PlayOutputVideoFragmentArgs.fromBundle(requireArguments()).videoPath
        initializePlayer(videoPath)
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)
        toolbar.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.release()
        exoPlayer = null
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)
        toolbar.visibility = View.VISIBLE
    }
}