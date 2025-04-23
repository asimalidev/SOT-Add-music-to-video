package com.addmusictovideos.audiovideomixer.sk.fragments

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentTrimVideoBinding
import com.addmusictovideos.audiovideomixer.sk.model.SharedViewModel
import com.addmusictovideos.audiovideomixer.sk.utils.FilePathManager
import com.addmusictovideos.audiovideomixer.sk.utils.OnVideoEditedEvent
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File


class TrimVideoFragment : Fragment(), OnVideoEditedEvent {
    private lateinit var binding: FragmentTrimVideoBinding
    private lateinit var seekBar: SeekBar
    private lateinit var volumetext: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var navController: NavController
    private lateinit var sharedViewModel: SharedViewModel
    private var volumeLevel: Float = 1.0f // Default volume (100%)

    companion object {
        private var isSaving = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrimVideoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            binding.videoTrimmer.releasePlayer()
            val action = TrimVideoFragmentDirections.actionCutvideoToAllvideo()
            navController.navigate(action)
        }

        setupUI()
        setupSeekBar()
        setupVideoTrimmer()
    }

    private fun setupUI() {
        seekBar = requireActivity().findViewById(R.id.simpleSeekBar)
        volumetext = requireActivity().findViewById(R.id.tvVolumePercentage)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        navController = findNavController()
        progressDialog = ProgressDialog(requireContext()).apply {
            setTitle("Cropping Video")
            setCancelable(false)
        }

        binding.btnSaveVideo.setOnClickListener {
            saveVideo()
        }
    }

    private fun setupSeekBar() {
        with(seekBar) {
            max = 100
            progress = 100
            isEnabled = true
            isClickable = true
            isFocusable = true

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        volumeLevel = progress / 100f
                        volumetext.text = "${(volumeLevel * 100).toInt()}%"
                        binding.videoTrimmer.setVolume(volumeLevel)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun setupVideoTrimmer() {
        val videoId = TrimVideoFragmentArgs.fromBundle(requireArguments()).videoPath
        val videoUri = Uri.fromFile(File(videoId))

        binding.videoTrimmer.apply {
            setVideoBackgroundColor(resources.getColor(R.color.black, null))
            setOnTrimVideoListener(this@TrimVideoFragment)
            setVideoURI(videoUri)
            setDestinationPath(FilePathManager(requireContext()).getTrimmedVideoParentDir())
            setVideoInformationVisibility(true)
            setMaxDuration(180)
            setMinDuration(0)
        }
    }

    private fun saveVideo() {
        if (isSaving) return

        isSaving = true
        progressDialog.show()

        binding.videoTrimmer.apply {
            setVolume(volumeLevel)
            saveVideo(volumeLevel)
            releasePlayer()
        }
    }

    override fun getResult(uri: Uri) {
        isSaving = false
        requireActivity().runOnUiThread {
            progressDialog.dismiss()
            Toast.makeText(requireContext(),"File cropped and saved at: ${uri.path}", Toast.LENGTH_SHORT).show()
        }

        val action = TrimVideoFragmentDirections.actionCutvideoToNavFinaloutput(uri.path.toString())
        navController.navigate(action)
    }

    override fun onError(message: String) {
        isSaving = false
        requireActivity().runOnUiThread {
            progressDialog.dismiss()
            Toast.makeText(requireContext(), "Something went wrong: $message", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.videoTrimmer.releasePlayer()
    }

    override fun onResume() {
        super.onResume()
        toggleUIVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        toggleUIVisibility(View.VISIBLE)
    }

    private fun toggleUIVisibility(visibility: Int) {
        requireActivity().findViewById<ConstraintLayout>(R.id.clToolbar).visibility = visibility
    }
}