package com.addmusictovideos.audiovideomixer.sk.fragments

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentCutVideoForAddMusicBinding
import com.addmusictovideos.audiovideomixer.sk.utils.FilePathManager
import com.addmusictovideos.audiovideomixer.sk.utils.OnVideoEditedEvent
import com.addmusictovideos.audiovideomixer.sk.utils.SharedViewModelForAddMusic
import java.io.File


class CutVideoForAddMusicFragment : Fragment(), OnVideoEditedEvent {
    private lateinit var binding: FragmentCutVideoForAddMusicBinding
    private lateinit var seekBar: SeekBar
    private lateinit var volumetext: TextView
    private lateinit var videopath:String
    private lateinit var audioPath:String
    private lateinit var progressDialog: ProgressDialog
    private lateinit var navController: NavController
    private lateinit var sharedViewModel: SharedViewModelForAddMusic
    private var volumeLevel: Float = 1.0f // Default volume (100%)


    companion object {
        private var isSaving = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCutVideoForAddMusicBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModelForAddMusic::class.java]
        videopath = CutVideoForAddMusicFragmentArgs.fromBundle(requireArguments()).videoPath
        audioPath = CutVideoForAddMusicFragmentArgs.fromBundle(requireArguments()).audioPath
        binding.ivBack.setOnClickListener {
            binding.videoTrimmer.releasePlayer()
            val action = CutVideoForAddMusicFragmentDirections.actionNavCutvideoToChangemusic(audioPath,videopath)
            navController.navigate(action)
        }

        setupUI()
        setupSeekBar()
        setupVideoTrimmer()
    }

    private fun setupUI() {
        seekBar = requireActivity().findViewById(R.id.simpleSeekBar)
        volumetext = requireActivity().findViewById(R.id.tvVolumePercentage)
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

        val videoUri = Uri.fromFile(File(videopath))

        binding.videoTrimmer.apply {
            setVideoBackgroundColor(resources.getColor(R.color.black, null))
            setOnTrimVideoListener(this@CutVideoForAddMusicFragment)
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
            Toast.makeText(requireContext(), "File cropped and saved at: ${uri.path}", Toast.LENGTH_SHORT).show()

            val newVideoPath = uri.path.toString() // Get new video path after trimming
            Log.e("CutVideo", "New video path: $newVideoPath")

            // Update the shared ViewModel with the new video path
            sharedViewModel.setVideoPath(newVideoPath)

            // Make sure we are properly updating the ViewModel data before navigation
            if (newVideoPath.isNotEmpty()) {
                val action = CutVideoForAddMusicFragmentDirections.actionNavCutvideoToChangemusic(audioPath, newVideoPath)
                Log.e("Navigation", "Navigating to ChangeMusicFragment with new video path: $newVideoPath")
                navController.navigate(action)
            } else {
                Log.e("Navigation", "New video path is empty, navigation not triggered")
            }
        }
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