package com.addmusictovideos.audiovideomixer.sk.AudioModule

import android.app.AlertDialog
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentExtractAudioConversionBinding
import com.addmusictovideos.audiovideomixer.sk.utils.FilePathManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textview.MaterialTextView
import java.io.File


class ExtractAudioConversionFragment : Fragment() {

    private lateinit var binding: FragmentExtractAudioConversionBinding
    private lateinit var videoPath: String
    private lateinit var outputPath: String
    private var durationInSeconds: Double = 0.0  // Store video duration

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentExtractAudioConversionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoPath = ExtractAudioConversionFragmentArgs.fromBundle(requireArguments()).videoPath
        binding.videoName.text = File(videoPath).name
        binding.progressBar.isClickable = false
        binding.progressBar.isFocusable = false
        binding.progressBar.isFocusableInTouchMode = false
        binding.progressBar.setOnTouchListener { _, _ -> true } // Blocks all touch events

        durationInSeconds = getVideoDuration(videoPath)

        convertVideoToMP3()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            })

        binding.ivBack.setOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_exit_confirmation, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<MaterialTextView>(R.id.textViewYes).setOnClickListener {
            val action = ExtractAudioConversionFragmentDirections.actionProgressToNavallvideo()
            findNavController().navigate(action)
            alertDialog.dismiss()
        }

        dialogView.findViewById<MaterialTextView>(R.id.textViewNo).setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun convertVideoToMP3() {
        outputPath = FilePathManager(requireContext()).getMP3ParentDir() + "/extract_audio_${System.currentTimeMillis()}.mp3"

        val cmd = arrayOf(
            "-y",
            "-i", "\"$videoPath\"",
            "-f",
            "mp3",
            "-ab",
            "192000",
            "-vn",
            outputPath)

        getDuration(videoPath) { totalDuration ->
            FFmpegKit.executeAsync(cmd.joinToString(" "), { session ->
                val state = session.state
                val returnCode = session.returnCode

                activity?.runOnUiThread {
                    context?.let { ctx ->
                        if (ReturnCode.isSuccess(returnCode)) {
                            Log.e("CheckingSuccess", "Success")
                            Toast.makeText(ctx, "MP3 saved at: $outputPath", Toast.LENGTH_SHORT).show()

                            val action = ExtractAudioConversionFragmentDirections.actionProgressToOutput(outputPath)
                            findNavController().navigate(action)
                        } else if (ReturnCode.isCancel(returnCode)) {
                            Log.e("CheckingCancel", "Cancelled")
                            Toast.makeText(ctx, "Conversion Cancelled!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(ctx, "Error !", Toast.LENGTH_SHORT).show()
                            Log.e("CheckingElse", String.format("FFMpeg process exited with state %s and rc %s.%s", state, returnCode, session.failStackTrace))
                        }
                    }
                }
            }, { log ->
                Log.d("Checking", "1: $log")
            }, { statistics ->
                val time = statistics.time
                if (totalDuration > 0) {
                    val progressPercentage = (time / totalDuration.toDouble()) * 100
                    Log.d("CheckingPerc", "Progress: ${progressPercentage.toInt()}%")
                    requireActivity().runOnUiThread {
                        binding.progressBar.progress = progressPercentage.toInt()
                        binding.progressText.text = "${progressPercentage.toInt()}%"
                    }
                }
            })
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        FFmpegKitConfig.enableLogCallback(null)
        FFmpegKitConfig.enableStatisticsCallback(null)
        FFmpegKit.cancel()
    }

    private fun getDuration(inputPath: String, callback: (Long) -> Unit) {
        FFprobeKit.getMediaInformationAsync(inputPath) { session ->
            val mediaInformation = session.mediaInformation
            val durationString = mediaInformation.duration
            val duration = durationString?.toDoubleOrNull() ?: 0.0
            callback((duration * 1000).toLong())
        }
    }

    private fun getVideoDuration(videoPath: String): Double {
        val retriever = MediaMetadataRetriever()
        return try {
            if (videoPath.startsWith("content://")) {
                // Handle content URI
                val context = requireContext() // or pass context as parameter
                context.contentResolver.openFileDescriptor(Uri.parse(videoPath), "r")?.use { pfd ->
                    retriever.setDataSource(pfd.fileDescriptor)
                }
            } else {
                // Handle file path
                val file = File(videoPath)
                if (!file.exists()) return 0.0
                retriever.setDataSource(videoPath)
            }

            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration?.toDoubleOrNull()?.div(1000) ?: 0.0
        } catch (e: Exception) {
            0.0
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore release exception
            }
        }
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