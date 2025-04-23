package com.addmusictovideos.audiovideomixer.sk.AudioModule

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
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentMergeAudioConversionBinding
import com.addmusictovideos.audiovideomixer.sk.model.Audio
import com.addmusictovideos.audiovideomixer.sk.utils.FilePathManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


class MergeAudioConversionFragment : Fragment() {
    private lateinit var binding: FragmentMergeAudioConversionBinding
    private lateinit var outputPath: String
    private var selectedAudioList: Array<Audio>? = null
    private lateinit var convertedAudioFiles: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMergeAudioConversionBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.let { MergeAudioConversionFragmentArgs.fromBundle(it) }
        selectedAudioList = args?.selectedVideos



        Log.d("MergeAudio", "Selected Audio List: $selectedAudioList")

        // Initialize converted file list
        convertedAudioFiles = mutableListOf()

        // Check audio formats and merge
        checkAudioFormatConsistency { isConsistent ->
            if (isConsistent) {
                mergeAudio()
            } else {
                convertAllToMP3 {
                    mergeAudio()
                }
            }
        }
    }

    private fun checkAudioFormatConsistency(callback: (Boolean) -> Unit) {
        val formats = mutableSetOf<String>()
        val countDownLatch = CountDownLatch(selectedAudioList?.size ?: 0)

        selectedAudioList?.forEach { audio ->
            FFprobeKit.getMediaInformationAsync(audio.path) { session ->
                val mediaInfo = session.mediaInformation
                val codec = mediaInfo?.streams?.firstOrNull()?.codec ?: "unknown"
                formats.add(codec)
                countDownLatch.countDown()
            }
        }

        thread {
            countDownLatch.await()
            callback(formats.size == 1) // True if all have the same format
        }
    }

    private fun convertAllToMP3(callback: () -> Unit) {
        val countDownLatch = CountDownLatch(selectedAudioList?.size ?: 0)

        selectedAudioList?.forEach { audio ->
            val outputMP3 = File(
                requireContext().cacheDir,
                "converted_${System.currentTimeMillis()}.mp3"
            ).absolutePath
            convertToMP3(audio.path, outputMP3) { success ->
                if (success) convertedAudioFiles.add(outputMP3)
                countDownLatch.countDown()
            }
        }

        thread {
            countDownLatch.await()
            callback()
        }
    }

    private fun convertToMP3(inputPath: String, outputPath: String, callback: (Boolean) -> Unit) {
        val cmd = arrayOf(
            "-y", "-i", inputPath,
            "-acodec", "libmp3lame",
            "-b:a", "192k",
            outputPath
        )

        FFmpegKit.executeAsync(cmd.joinToString(" ")) { session ->
            val returnCode = session.returnCode
            callback(ReturnCode.isSuccess(returnCode))
        }
    }

    private fun mergeAudio() {
        if (!isAdded) return
        val filePathManager = FilePathManager(requireContext())
        outputPath = filePathManager.getmergeAudioDirectory() + "audio_merge_${System.currentTimeMillis()}.mp3"


        val inputFilePaths = createInputFileList()

        val cmd = arrayListOf(
            "-y",
            "-f", "concat",
            "-safe", "0",
            "-i", "\"$inputFilePaths\"",
            "-c:a", "copy", // Keep original codec to avoid unnecessary conversion
            outputPath
        )

        getDurationForMerge { totalDuration ->
            FFmpegKit.executeAsync(cmd.joinToString(" "), { session ->
                val state = session.state
                val returnCode = session.returnCode

                activity?.runOnUiThread {
                    context?.let { ctx ->
                        if (ReturnCode.isSuccess(returnCode)) {
                            Log.e("MergeSuccess", "Audio Merged Successfully")
                            Toast.makeText(
                                ctx,
                                "Merged Audio saved at: $outputPath",
                                Toast.LENGTH_SHORT
                            ).show()

                            val action = MergeAudioConversionFragmentDirections
                                .actionMergeAudioProgressFragmentToFinaloutput(
                                    outputPath,
                                    outputPath
                                )
                            findNavController().navigate(action)
                        } else {
                            Log.e(
                                "MergeFailed",
                                "FFMpeg exited with state $state and rc $returnCode"
                            )
                            Toast.makeText(ctx, "Audio merge failed! use either a portrait videos or landscape", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }, { log ->
                Log.d("FFmpegLog", log.message)
            }, { statistics ->
                val time = statistics.time // This is the current time of the process
                if (totalDuration > 0) {
                    // Update progress percentage
                    val progressPercentage = (time / totalDuration.toDouble()) * 100
                    Log.d("MergeProgress", "Progress: ${progressPercentage.toInt()}%")

                    requireActivity().runOnUiThread {
                        binding.progressBar.progress = progressPercentage.toInt()
                        binding.progressText.text = "${progressPercentage.toInt()}%"
                    }
                }
            })
        }
    }


    private fun getDurationForMerge(callback: (Long) -> Unit) {
        val durations = mutableListOf<Double>()
        val countDownLatch = CountDownLatch(selectedAudioList?.size ?: 0)

        selectedAudioList?.forEach { audio ->
            FFprobeKit.getMediaInformationAsync(audio.path) { session ->
                val mediaInfo = session.mediaInformation
                val durationString = mediaInfo?.duration ?: "0"
                durations.add(durationString.toDoubleOrNull() ?: 0.0)
                countDownLatch.countDown()
            }
        }

        thread {
            countDownLatch.await()
            val totalDuration = durations.sum()
            callback((totalDuration * 1000).toLong()) // Convert to milliseconds
        }
    }

    private fun createInputFileList(): String {
        val tempFile = File(requireContext().cacheDir, "audio_input.txt")
        if (tempFile.exists()) tempFile.delete()

        val audioFiles =
            if (convertedAudioFiles.isNotEmpty()) convertedAudioFiles else selectedAudioList?.map { it.path }

        audioFiles?.let { list ->
            val fileContents = list.joinToString("\n") { "file '${it.replace("\\", "/")}'" }
            tempFile.writeText(fileContents)
        }

        return tempFile.absolutePath
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