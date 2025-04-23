package com.addmusictovideos.audiovideomixer.sk.fragments

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentChangeMusicProgressBinding
import com.addmusictovideos.audiovideomixer.sk.utils.FilePathManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File


class ChangeMusicProgressFragment : Fragment() {
    private lateinit var binding: FragmentChangeMusicProgressBinding
    private lateinit var audioPath: String
    private lateinit var videoPath: String
    private lateinit var outputPath: String
    private var videoDuration: Double = 0.0 // Store video duration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeMusicProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioPath = ChangeMusicProgressFragmentArgs.fromBundle(requireArguments()).audioPath
        videoPath = ChangeMusicProgressFragmentArgs.fromBundle(requireArguments()).videoPath
        binding.videoName.text = File(videoPath).name

        // Get video duration to adjust progress and looping logic
        videoDuration = getVideoDuration(videoPath)

        binding.progressBar.max = 100 // Set max to 100 for percentage progress
        binding.progressBar.isIndeterminate = false

        convertChangeMusic()
    }


    private fun convertChangeMusic() {
        outputPath =
            FilePathManager(requireContext()).getChangeMusicVideoParentDir() + "/change_music_${System.currentTimeMillis()}.mp4"

        val cmd = arrayOf(
            "-y", // Overwrite output file if exists
            "-i", "\"$videoPath\"", // Input video
            "-i", "\"$audioPath\"", // Input audio
            "-an", // Disable video audio (mute video)
            "-filter_complex",
            "[1]aloop=loop=-1:size=2e+09[looped];[0][looped]amix=inputs=2:duration=longest", // Loop audio if it's shorter than video
            "-c:v",
            "libx264",
            "-preset",
            "ultrafast",
            "-c:a",
            "aac",
            "-b:a",
            "192k",
            "-shortest", // Ensure the output video is the same length as the video
            "\"$outputPath\""
        )

        getDuration(videoPath) { totalDuration ->
            FFmpegKit.executeAsync(cmd.joinToString(" "), { session ->
                val state = session.state
                val returnCode = session.returnCode

                activity?.runOnUiThread {
                    context?.let { ctx ->
                        if (ReturnCode.isSuccess(returnCode)) {
                            Log.e("CheckingSuccess", "Success")
                            Toast.makeText(
                                ctx,
                                "Merged video saved at: $outputPath",
                                Toast.LENGTH_SHORT
                            ).show()
                            val action =
                                ChangeMusicProgressFragmentDirections.actionNavChangemusicprogressToNavFinaloutput(
                                    outputPath
                                )
                            findNavController().navigate(action)
                        } else if (ReturnCode.isCancel(returnCode)) {
                            Log.e("CheckingCancel", "Cancelled")
                            Toast.makeText(ctx, "Conversion Cancelled!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(ctx, "Conversion Failed!", Toast.LENGTH_SHORT).show()
                            Log.e(
                                "CheckingElse",
                                String.format(
                                    "FFMpeg process exited with state %s and rc %s.%s",
                                    state,
                                    returnCode,
                                    session.failStackTrace
                                )
                            )
                        }
                    }
                }
            }, { log ->
                Log.d("Checking", "1: $log")
            }, { statistics ->
                val time = statistics.time
                if (totalDuration > 0) {
                    // Calculate progress percentage (from 1 to 100)
                    val progressPercentage =
                        ((time / totalDuration.toDouble()) * 100).coerceIn(1.0, 100.0)
                    Log.d("CheckingPerc", "Progress: ${progressPercentage.toInt()}%")

                    requireActivity().runOnUiThread {
                        binding.progressBar.progress = progressPercentage.toInt()
                        binding.progressText.text = "${progressPercentage.toInt()}%"
                    }
                }
            })
        }
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
        retriever.setDataSource(videoPath)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        return duration?.toDouble()?.div(1000) ?: 0.0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FFmpegKitConfig.enableLogCallback(null)
        FFmpegKitConfig.enableStatisticsCallback(null)
        FFmpegKit.cancel()
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