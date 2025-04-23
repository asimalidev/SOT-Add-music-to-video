package com.addmusictovideos.audiovideomixer.sk.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentMergeVideoConversionBinding
import com.addmusictovideos.audiovideomixer.sk.model.AudioFile
import com.addmusictovideos.audiovideomixer.sk.model.Video
import com.addmusictovideos.audiovideomixer.sk.utils.FilePathManager
import com.addmusictovideos.audiovideomixer.sk.utils.LayoutStyle
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textview.MaterialTextView


class MergeVideoConversionFragment : Fragment() {
    private lateinit var binding: FragmentMergeVideoConversionBinding
    private lateinit var outputPath: String
    private lateinit var mergestyle: String
    private lateinit var navController: NavController
    private var durationInSeconds: Double = 0.0
    private var selectedVideos: List<Video>? = null
    private var aspectRatio: String = "original" // default
    private var resolution: String = "medium"    // default
    private var selectedAudios: List<AudioFile> = emptyList()
    private var muteOriginalAudio: Boolean = false
    private var selectedLayoutStyle: LayoutStyle = LayoutStyle.SEQUENTIAL


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMergeVideoConversionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        val args = MergeVideoConversionFragmentArgs.fromBundle(requireArguments())
        selectedVideos = arguments?.let {
            MergeVideoFragmentArgs.fromBundle(it).selectedVideos.toList()
        }
        mergestyle = args.mergeStyle
        aspectRatio = args.aspectRatio
        resolution = args.resolution
        selectedAudios = args.selectedAudios.toList()

        Log.e("selected audio", "selected audios are ${selectedAudios.size}")

        Log.e(
            "selection",
            "aspect ratio is $aspectRatio, resolution=$resolution, audio are $selectedAudios, muteOriginalAudio=$muteOriginalAudio"
        )

        selectedVideos?.let { videos ->
            durationInSeconds = videos.sumOf { getVideoDuration(it.path) }
            mergeVideos(videos)
        } ?: run {
            Toast.makeText(requireContext(), "No videos selected", Toast.LENGTH_SHORT).show()
        }

        binding.progressBar.isClickable = false
        binding.progressBar.isFocusable = false
        binding.progressBar.isFocusableInTouchMode = false
        binding.progressBar.setOnTouchListener { _, _ -> true }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            }
        )

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
            val action = MergeVideoConversionFragmentDirections.actionNavProgressVideoToNavAllvideos()
            findNavController().navigate(action)
            alertDialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.textViewNo).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
    private fun getFFmpegCommand(videos: List<Video>, outputPath: String): String {
        val cmdArray = ArrayList<String>().apply {
            add("-y")
            add("-progress")
            add("pipe:1")
        }

        // Add video inputs
        videos.forEach { video ->
            cmdArray.add("-i")
            cmdArray.add(video.path)
        }

        // Add all selected audio inputs
        selectedAudios.forEach { audio ->
            cmdArray.add("-i")
            cmdArray.add(audio.path)
        }

        // Build filter complex
        val filterComplex = StringBuilder()

        // Video processing (unchanged)
        val (aspectW, aspectH) = getAspectRatioValues(aspectRatio)
        val (outputWidth, outputHeight) = getOutputDimensions(aspectW, aspectH)
        for (i in videos.indices) {
            filterComplex.append("[$i:v]")
            if (aspectRatio == "original") {
                filterComplex.append("scale=w=$outputWidth:h=$outputHeight:force_original_aspect_ratio=decrease,")
            } else {
                filterComplex.append("scale=w=$outputWidth:h=$outputHeight:force_original_aspect_ratio=decrease,")
                filterComplex.append("pad=$outputWidth:$outputHeight:(ow-iw)/2:(oh-ih)/2,")
            }
            filterComplex.append("setsar=1[v$i];")
        }

        // Audio handling for sequential background audios
        if (selectedAudios.isNotEmpty()) {
            // Process each background audio
            selectedAudios.forEachIndexed { index, _ ->
                val audioIndex = videos.size + index
                filterComplex.append("[$audioIndex:a]asetpts=N/SR/TB[bg$index];")
            }

            // Concatenate all background audios sequentially
            filterComplex.append(selectedAudios.indices.joinToString("") { "[bg$it]" })
            filterComplex.append("concat=n=${selectedAudios.size}:v=0:a=1[a];")
        } else {
            // If no background audio selected, keep original audio
            filterComplex.append("${videos.indices.joinToString("") { "[$it:a]" }}concat=n=${videos.size}:v=0:a=1[a];")
        }

        // Video concatenation (unchanged)
        filterComplex.append("${videos.indices.joinToString("") { "[v$it]" }}concat=n=${videos.size}:v=1:a=0[v]")

        cmdArray.add("-filter_complex")
        cmdArray.add(filterComplex.toString())

        // Output options
        cmdArray.addAll(
            listOf(
                "-map", "[v]",
                "-map", "[a]",
                "-movflags", "+faststart",
                "-vsync", "2",
                "-c:v", "libx264",
                "-preset", getPresetValue(),
                "-crf", getCrfValue(),
                "-c:a", "aac",
                "-b:a", "192k",
                "-shortest",
                outputPath
            )
        )

        Log.d("FFmpegCommand", cmdArray.joinToString(" "))
        return cmdArray.joinToString(" ")
    }

    private fun createDuetVideo(videos: List<Video>, outputPath: String): String {
        if (videos.size != 2) {
            Toast.makeText(requireContext(), "Duet requires exactly 2 videos", Toast.LENGTH_SHORT)
                .show()
            return ""
        }

        val cmdArray = ArrayList<String>().apply {
            add("-y")
            add("-progress")
            add("pipe:1")
        }

        // Add both video inputs
        videos.forEach { video ->
            cmdArray.add("-i")
            cmdArray.add(video.path)
        }

        // Add audio inputs if needed
        selectedAudios.forEach { audio ->
            cmdArray.add("-i")
            cmdArray.add(audio.path)
        }

        // Build filter complex
        val filterComplex = StringBuilder()

        // Scale both videos to same size (half of output for side-by-side)
        val (outputWidth, outputHeight) = when (aspectRatio) {
            "9:16" -> Pair(720, 1280) // Vertical duet
            else -> Pair(1280, 720)    // Horizontal duet (default)
        }

        val scaledWidth = if (aspectRatio == "9:16") outputWidth else outputWidth / 2
        val scaledHeight = if (aspectRatio == "9:16") outputHeight / 2 else outputHeight

        filterComplex.append("[0:v]scale=$scaledWidth:$scaledHeight:force_original_aspect_ratio=decrease,setsar=1[v0];")

        filterComplex.append("[1:v]scale=$scaledWidth:$scaledHeight:force_original_aspect_ratio=decrease,setsar=1[v1];")


        // Combine videos based on duet style
        if (aspectRatio == "9:16") {
            // Vertical duet (top-bottom)
            filterComplex.append("[v0][v1]vstack=inputs=2[v];")
        } else {
            // Horizontal duet (side-by-side)
            filterComplex.append("[v0][v1]hstack=inputs=2[v];")
        }

        // Audio handling (use first video's audio by default)
        filterComplex.append("[0:a]volume=1.0[a];")

        cmdArray.add("-filter_complex")
        cmdArray.add(filterComplex.toString())

        // Output options
        cmdArray.addAll(
            listOf(
                "-map", "[v]",
                "-map", "[a]",
                "-movflags", "+faststart",
                "-c:v", "libx264",
                "-preset", "fast",
                "-crf", "20",
                "-c:a", "aac",
                "-b:a", "192k",
                outputPath
            )
        )

        Log.d("DuetCommand", cmdArray.joinToString(" "))
        return cmdArray.joinToString(" ")
    }

    private fun createVerticalDuetVideo(videos: List<Video>, outputPath: String): String {
        if (videos.size != 2) {
            Toast.makeText(requireContext(), "Duet requires exactly 2 videos", Toast.LENGTH_SHORT).show()
            return ""
        }

        val cmdArray = ArrayList<String>().apply {
            add("-y")
            add("-progress")
            add("pipe:1")
        }

        videos.forEach { video ->
            cmdArray.add("-i")
            cmdArray.add(video.path)
        }

        // Add audios if needed
        selectedAudios.forEach { audio ->
            cmdArray.add("-i")
            cmdArray.add(audio.path)
        }

        // Get output resolution
        val outputWidth = 720
        val outputHeight = 1280
        val scaledWidth = outputWidth
        val scaledHeight = outputHeight / 2

        val filterComplex = StringBuilder()
        filterComplex.append("[0:v]scale=$scaledWidth:$scaledHeight:force_original_aspect_ratio=decrease,setsar=1[v0];")
        filterComplex.append("[1:v]scale=$scaledWidth:$scaledHeight:force_original_aspect_ratio=decrease,setsar=1[v1];")
        filterComplex.append("[v0][v1]vstack=inputs=2[v];")

        // Audio
        filterComplex.append("[0:a]volume=1.0[a];")

        cmdArray.add("-filter_complex")
        cmdArray.add(filterComplex.toString())

        cmdArray.addAll(
            listOf(
                "-map", "[v]",
                "-map", "[a]",
                "-movflags", "+faststart",
                "-c:v", "libx264",
                "-preset", "fast",
                "-crf", "20",
                "-c:a", "aac",
                "-b:a", "192k",
                outputPath
            )
        )

        Log.d("VerticalDuetCommand", cmdArray.joinToString(" "))
        return cmdArray.joinToString(" ")
    }


    private fun mergeVideos(videos: List<Video>) {

        val fileNamePrefix = when (mergestyle) {
            "Horizontally", "Vertically" -> "duet"
            else -> "merged"
        }

        outputPath = FilePathManager(requireContext()).getMergeVideoParentDir() +
                "/${fileNamePrefix}_${System.currentTimeMillis()}.mp4"

        binding.videoName.text = outputPath

        val cmd = when (mergestyle) {
            "Sequentially" -> getFFmpegCommand(videos, outputPath)
            "Horizontally" -> {
                if (videos.size == 2) createDuetVideo(videos, outputPath)
                else {
                    Toast.makeText(requireContext(), "Duet requires exactly 2 videos", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            "Vertically" -> {
                if (videos.size == 2) createVerticalDuetVideo(videos, outputPath)
                else {
                    Toast.makeText(requireContext(), "Duet requires exactly 2 videos", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            else -> getFFmpegCommand(videos, outputPath)
        }
        FFmpegKit.executeAsync(cmd, { session ->
            val state = session.state
            val returnCode = session.returnCode

            requireActivity().runOnUiThread {
                if (ReturnCode.isSuccess(returnCode)) {
                    val successMessage =
                        if (videos.size == 2) "Duet created successfully!" else "Videos merged successfully!"
                    Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
                    val action =
                        MergeVideoConversionFragmentDirections.actionNavProgressVideoToNavOutputProgress(
                            outputPath
                        )
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Failed to process videos", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("FFmpegError", session.failStackTrace ?: "Unknown error")
                }
            }
        }, { log ->
            Log.d("FFmpegLog", "$log")
        }, { stats ->
            if (durationInSeconds > 0) {
                val progress =
                    ((stats.time / 1000.0) / durationInSeconds * 100).toInt().coerceAtMost(100)
                requireActivity().runOnUiThread {
                    binding.progressBar.progress = progress
                    binding.progressText.text = "$progress%"
                }
            }
        })

    }


    private fun getAspectRatioValues(aspectRatio: String): Pair<Int, Int> {
        return when (aspectRatio) {
            "16:9" -> Pair(16, 9)
            "9:16" -> Pair(9, 16)
            "1:1" -> Pair(1, 1)
            "5:4" -> Pair(5, 4)
            "4:5" -> Pair(4, 5)
            "4:3" -> Pair(4, 3)
            "3:4" -> Pair(3, 4)
            else -> Pair(-1, -1) // original aspect ratio
        }
    }

    private fun getOutputDimensions(aspectW: Int, aspectH: Int): Pair<Int, Int> {
        if (aspectRatio == "original") {
            // Use the resolution of the first video as base
            val firstVideoPath = selectedVideos?.firstOrNull()?.path ?: return Pair(1280, 720)
            val (width, height) = getVideoResolution(firstVideoPath)
            return when (resolution) {
                "small" -> Pair((width * 0.5).toInt(), (height * 0.5).toInt())
                "medium" -> Pair(width, height)
                "large" -> Pair((width * 1.5).toInt(), (height * 1.5).toInt())
                else -> Pair(width, height)
            }
        }

        return when (resolution) {
            "small" -> {
                val height = 480
                val width = (height * aspectW / aspectH)
                Pair(width, height)
            }

            "medium" -> {
                val height = 720
                val width = (height * aspectW / aspectH)
                Pair(width, height)
            }

            "large" -> {
                val height = 1080
                val width = (height * aspectW / aspectH)
                Pair(width, height)
            }

            else -> {
                val height = 720
                val width = (height * aspectW / aspectH)
                Pair(width, height)
            }
        }
    }

    private fun getVideoResolution(videoPath: String): Pair<Int, Int> {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        val width =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
                ?: 1280
        val height =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                ?: 720
        retriever.release()
        return Pair(width, height)
    }

    private fun getCrfValue(): String = when (resolution) {
        "small" -> "23"
        "medium" -> "20"
        "large" -> "18"
        else -> "20"
    }

    private fun getPresetValue(): String = when (resolution) {
        "large" -> "slow"
        else -> "medium"
    }

    private fun getVideoDuration(videoPath: String): Double {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        return duration?.toDouble()?.div(1000) ?: 0.0
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