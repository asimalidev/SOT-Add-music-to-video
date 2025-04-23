package com.addmusictovideos.audiovideomixer.sk.AudioModule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.AlertDialog
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentExtractAudioBinding
import com.google.android.material.textview.MaterialTextView

class ExtractAudioFragment : Fragment() {
    private lateinit var binding: FragmentExtractAudioBinding
    private var exoPlayer: ExoPlayer? = null
    private lateinit var videoPath: String
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentExtractAudioBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController=findNavController()
        binding.btnconvert.setOnClickListener {
            val action = ExtractAudioFragmentDirections.actionCutvideoToExtractAudioProgress(videoPath)
            navController.navigate(action)
        }

        binding.ivBack.setOnClickListener {
            showExitConfirmationDialog()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            }
        )
    }

    private fun showExitConfirmationDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_exit_confirmation, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<MaterialTextView>(R.id.textViewYes).setOnClickListener {
            val action =  ExtractAudioFragmentDirections.actionExtractaudioToHome()
            findNavController().navigate(action)
            alertDialog.dismiss()
        }

        dialogView.findViewById<MaterialTextView>(R.id.textViewNo).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
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
        videoPath = ExtractAudioFragmentArgs.fromBundle(requireArguments()).videoPath
        initializePlayer(videoPath)
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)
        toolbar.visibility = View.GONE }

    override fun onPause() {
        super.onPause()
        exoPlayer?.release()
        exoPlayer = null
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)
        toolbar.visibility = View.VISIBLE
    }
}