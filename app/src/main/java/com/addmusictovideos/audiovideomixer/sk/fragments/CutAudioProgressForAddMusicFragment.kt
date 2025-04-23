package com.addmusictovideos.audiovideomixer.sk.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.addmusictovideos.audiovideomixer.sk.R


import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentCutAudioProgressForAddMusicBinding
import com.addmusictovideos.audiovideomixer.sk.utils.FilePathManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File


class CutAudioProgressForAddMusicFragment : Fragment() {

    private lateinit var binding: FragmentCutAudioProgressForAddMusicBinding
    private lateinit var audioPath: String
    private lateinit var videoPath: String
    private var startTime: Long = 0L
    private var endTime: Long = 0L
    private var speed: Float = 1.0f
    private var selectedVolume: Int = 100
    private lateinit var outputPath: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCutAudioProgressForAddMusicBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = CutAudioProgressForAddMusicFragmentArgs.fromBundle(requireArguments())
        val sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        selectedVolume = sharedPreferences.getInt("selected_volume", 100)
        speed = sharedPreferences.getFloat("selectedSpeed", 1.0f)



        Log.e("selectedVolume", " value of selected volume is $selectedVolume")
        videoPath=args.videoPath

        audioPath = args.audioPath
        startTime = args.startTime
        endTime = args.endTime

        val audioFileName = File(audioPath).name

        // Set the audio name to the TextView
        binding.videoName.text = audioFileName

        Log.e("TrimAudioProgressFragment", "Audio path: $audioPath")
        Log.e("TrimAudioProgressFragment", "Start Time: $startTime, End Time: $endTime")
        Log.e("TrimAudioProgressFragment", "Volume: $selectedVolume, Speed: $speed")

        trimAudio()
    }


    private fun trimAudio() {
        outputPath = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Android 10 (Scoped Storage)
            File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "trimmed_audio_${System.currentTimeMillis()}.mp3").absolutePath
        } else {
            // Other Android versions (Public directory)
            FilePathManager(requireContext()).getTrimAudioParentDir() +
                    "/trimmed_audio_${System.currentTimeMillis()}.mp3"
        }

        val volumeFilter = "volume=${selectedVolume / 100.0}"
        val speedFilter = if (speed in 0.5..2.0) {
            "atempo=$speed"
        } else {
            // Chain multiple atempo filters if speed is out of range
            if (speed > 2.0) {
                "atempo=2.0,atempo=${speed / 2.0}"
            } else {
                "atempo=0.5,atempo=${speed / 0.5}"
            }
        }

        val cmd = arrayListOf(
            "-y",
            "-i", "\"$audioPath\"",
            "-ss", (startTime / 1000).toString(),
            "-to", (endTime / 1000).toString(),
            "-filter_complex", "[0:a]$volumeFilter,$speedFilter[a]",
            "-map", "[a]",
            "-b:a", "192k", // Keep audio quality good
            outputPath
        )

        getDuration(audioPath) { totalDuration ->
            FFmpegKit.executeAsync(cmd.joinToString(" "), { session ->
                val state = session.state
                val returnCode = session.returnCode

                activity?.runOnUiThread {
                    context?.let { ctx ->
                        if (ReturnCode.isSuccess(returnCode)) {
                            Log.e("CheckingSuccess", "Success")
                            Toast.makeText(ctx, "Audio saved at: $outputPath", Toast.LENGTH_SHORT).show()
                            val action = CutAudioProgressForAddMusicFragmentDirections.actionTrimaudioprogressaddmusicToNavChangemusic(videoPath,audioPath)
                            findNavController().navigate(action)
                        } else if (ReturnCode.isCancel(returnCode)) {
                            Log.e("CheckingCancel", "Cancelled")
                            Toast.makeText(ctx, "Conversion Cancelled!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(ctx, "Error !", Toast.LENGTH_SHORT).show()
                            Log.e("CheckingElse", String.format(
                                "FFMpeg process exited with state %s and rc %s.%s",
                                state, returnCode, session.failStackTrace
                            ))
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