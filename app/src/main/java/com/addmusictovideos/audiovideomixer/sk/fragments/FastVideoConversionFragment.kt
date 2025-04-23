package com.addmusictovideos.audiovideomixer.sk.fragments

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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentFastVideoBinding
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentFastVideoConversionBinding
import com.addmusictovideos.audiovideomixer.sk.utils.FilePathManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textview.MaterialTextView
import java.io.File


class FastVideoConversionFragment : Fragment() {
    private lateinit var binding: FragmentFastVideoConversionBinding
    private lateinit var outputPath: String
    private lateinit var navController: NavController
    private var durationInSeconds: Double = 0.0
    private var videoPath: String = ""
    private var selectedSpeed: Float = 1.0f
    private var isMuted: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFastVideoConversionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        arguments?.let {
            val args = FastVideoConversionFragmentArgs.fromBundle(it)
            videoPath = args.videoPath
            selectedSpeed = args.speed
            isMuted = args.isMuted
        }

        Log.e("ismuted", "value of is mutes is $isMuted")

        binding.progressBar.isClickable = false
        binding.progressBar.isFocusable = false
        binding.progressBar.isFocusableInTouchMode = false
        binding.progressBar.setOnTouchListener { _, _ -> true }

        durationInSeconds = getVideoDuration(videoPath)
        adjustVideoSpeed()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            })
    }

    private fun showExitConfirmationDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_exit_confirmation, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<MaterialTextView>(R.id.textViewYes).setOnClickListener {
            val action = FastVideoConversionFragmentDirections.actionNavSpeedProgressToNavAllvideo()
            findNavController().navigate(action)
            alertDialog.dismiss()
        }

        dialogView.findViewById<MaterialTextView>(R.id.textViewNo).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
    private fun adjustVideoSpeed() {
        outputPath = FilePathManager(requireContext()).getFastSpeedParentDir() + "/fast_speed_${System.currentTimeMillis()}.mp4"

        binding.videoName.text = outputPath

        val speedFactor = when (selectedSpeed) {
            1.25f -> "setpts=0.8*PTS"
            1.5f -> "setpts=0.6667*PTS"
            1.75f -> "setpts=0.5714*PTS"
            2.0f -> "setpts=0.5*PTS"
            else -> "setpts=1.0*PTS"
        }

        val cmd = mutableListOf("-y", "-i", "\"$videoPath\"", "-filter_complex")
        val filterGraph = if (isMuted) {
            "[0:v]$speedFactor[v]"
        } else {
            val atempoFilter = when (selectedSpeed) {
                1.0f -> "atempo=1.0"
                1.25f -> "atempo=1.25"
                1.5f -> "atempo=1.5"
                1.75f -> "atempo=1.75"
                2.0f -> "atempo=2.0"
                else -> "atempo=1.0"
            }
            "[0:v]$speedFactor[v];[0:a]$atempoFilter[a]"
        }
        cmd.add(filterGraph)
        cmd.add("-map")
        cmd.add("[v]")
        if (!isMuted) {
            cmd.add("-map")
            cmd.add("[a]")
        } else {
            cmd.add("-an")
        }
        cmd.add("-r")
        cmd.add("60")
        cmd.add("-vcodec")
        cmd.add("mpeg4")
        cmd.add(outputPath)

        getDuration(videoPath) { totalDuration ->
            val adjustedDuration = (totalDuration / selectedSpeed).toLong()

            FFmpegKit.executeAsync(cmd.joinToString(" "), { session ->
                val state = session.state
                val returnCode = session.returnCode

                activity?.runOnUiThread {
                    context?.let { ctx ->
                        if (ReturnCode.isSuccess(returnCode)) {
                            Log.e("CheckingSuccess", "Success")
                            Toast.makeText(ctx, "Speed changed successfully!", Toast.LENGTH_SHORT).show()

                            val action = FastVideoConversionFragmentDirections.actionNavSpeedProgressToFinalOutput(outputPath)
                            navController.navigate(action)
                        } else if (ReturnCode.isCancel(returnCode)) {
                            Log.e("CheckingCancel", "Cancelled")
                            Toast.makeText(ctx, "Conversion Cancelled!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(ctx, "Error !", Toast.LENGTH_SHORT).show()
                            Log.e("CheckingElse", "FFmpeg failed: ${session.failStackTrace}")
                        }
                    }
                }
            }, { log ->
                Log.d("FFmpegLog", log.toString())
            }, { statistics ->
                val time = statistics.time
                if (adjustedDuration > 0) {
                    val progressPercentage = (time.toDouble() / adjustedDuration) * 100
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

    override fun onDestroyView() {
        super.onDestroyView()
        FFmpegKitConfig.enableLogCallback(null)
        FFmpegKitConfig.enableStatisticsCallback(null)
        FFmpegKit.cancel()
    }

    private fun toggleUIVisibility(visibility: Int) {
        requireActivity().findViewById<ConstraintLayout>(R.id.clToolbar).visibility = visibility
    }
}