package com.addmusictovideos.audiovideomixer.sk.fragments

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.adapter.ExtractAudioAdapter
import com.addmusictovideos.audiovideomixer.sk.adapter.VideoMergeAdapter
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentMergeVideoBinding
import com.addmusictovideos.audiovideomixer.sk.databinding.NewMergeOptionsBinding
import com.addmusictovideos.audiovideomixer.sk.model.AudioFile
import com.addmusictovideos.audiovideomixer.sk.model.SharedViewModel
import com.addmusictovideos.audiovideomixer.sk.model.Video


import com.addmusictovideos.audiovideomixer.sk.utils.MergeVideoHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer


class MergeVideoFragment : Fragment() {
    private lateinit var audioAdapter: ExtractAudioAdapter
    private lateinit var navController: NavController
    private var selectedResolution: String = "medium" // default
    private lateinit var mergebinding: NewMergeOptionsBinding
    private lateinit var binding: FragmentMergeVideoBinding
    private var selectedAspectRatio: Pair<Int, Int>? = null
    private lateinit var sharedViewModel: SharedViewModel
    private var selectedVideos: List<Video>? = null
    private val videoUtils = MergeVideoHelper()
    private var currentPlayingVideo: Video? = null
    private var player: Player? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMergeVideoBinding.inflate(inflater, container, false)
        navController = findNavController()
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        arguments?.let {
            selectedVideos = MergeVideoFragmentArgs.fromBundle(it).selectedVideos.toList()
        }

        mergebinding = NewMergeOptionsBinding.bind(binding.mergeOptionsMerge.root)
        setupUI()

        if (selectedVideos?.size == 2 && haveSameOrientation(selectedVideos!!)) {
            binding.mergeOptionsMerge.rbUpDown.visibility = View.VISIBLE
            binding.mergeOptionsMerge.rbLeftRight.visibility = View.VISIBLE
        } else {
            binding.mergeOptionsMerge.rbUpDown.visibility = View.GONE
            binding.mergeOptionsMerge.rbLeftRight.visibility = View.GONE
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    releasePlayer()
                    findNavController().navigateUp()
                }
            })

        binding.ivAdd.setOnClickListener {
            binding.ivAdd.setOnClickListener {
                val action = MergeVideoFragmentDirections.actionMergevideoToAllvideo()
                navController.navigate(action)
            }
        }

        binding.tvRatio.setOnClickListener {
            binding.ratioOptionsMerge.clRatioOptions.visibility = View.VISIBLE
            binding.resolutionOptionMerge.clQualitySelection.visibility = View.GONE
            binding.audioOption.clAudioOptions.visibility = View.GONE
            binding.mergeOptionsMerge.clMergeOption.visibility = View.GONE
        }

        binding.tvMergeStyle.setOnClickListener {
            binding.mergeOptionsMerge.clMergeOption.visibility = View.VISIBLE
            binding.ratioOptionsMerge.clRatioOptions.visibility = View.GONE
            binding.resolutionOptionMerge.clQualitySelection.visibility = View.GONE
            binding.audioOption.clAudioOptions.visibility = View.GONE

            val selectedColor = ContextCompat.getColor(requireContext(), R.color.appcolor)
            val defaultColor = ContextCompat.getColor(requireContext(), R.color.white)


            // Set selected for tvResolution
            binding.tvMergeStyle.setTextColor(selectedColor)
            binding.tvMergeStyle.compoundDrawablesRelative[0]?.mutate()?.setTint(selectedColor)

            binding.tvResolution.setTextColor(defaultColor)
            binding.tvResolution.compoundDrawablesRelative[0]?.mutate()?.setTint(defaultColor)

            // Reset others
            binding.tvAudio.setTextColor(defaultColor)
            binding.tvAudio.compoundDrawablesRelative[0]?.mutate()?.setTint(defaultColor)

            binding.tvRatio.setTextColor(defaultColor)
            binding.tvRatio.compoundDrawablesRelative[0]?.mutate()?.setTint(defaultColor)
        }

        binding.tvResolution.setOnClickListener {
            // Show resolution options and hide others
            binding.resolutionOptionMerge.clQualitySelection.visibility = View.VISIBLE
            binding.mergeOptionsMerge.clMergeOption.visibility = View.GONE
            binding.ratioOptionsMerge.clRatioOptions.visibility = View.GONE
            binding.audioOption.clAudioOptions.visibility = View.GONE


            val selectedColor = ContextCompat.getColor(requireContext(), R.color.appcolor)
            val defaultColor = ContextCompat.getColor(requireContext(), R.color.white)

            // Set selected for tvResolution
            binding.tvResolution.setTextColor(selectedColor)
            binding.tvResolution.compoundDrawablesRelative[0]?.mutate()?.setTint(selectedColor)

            binding.tvMergeStyle.setTextColor(defaultColor)
            binding.tvMergeStyle.compoundDrawablesRelative[0]?.mutate()?.setTint(defaultColor)

            // Reset others
            binding.tvAudio.setTextColor(defaultColor)
            binding.tvAudio.compoundDrawablesRelative[0]?.mutate()?.setTint(defaultColor)

            binding.tvRatio.setTextColor(defaultColor)
            binding.tvRatio.compoundDrawablesRelative[0]?.mutate()?.setTint(defaultColor)
        }

        binding.tvRatio.setOnClickListener {
            // Show ratio options and hide others
            binding.ratioOptionsMerge.clRatioOptions.visibility = View.VISIBLE
            binding.mergeOptionsMerge.clMergeOption.visibility = View.GONE
            binding.resolutionOptionMerge.clQualitySelection.visibility = View.GONE
            binding.audioOption.clAudioOptions.visibility = View.GONE

            // Colors
            val selectedColor = ContextCompat.getColor(requireContext(), R.color.appcolor)
            val defaultColor = ContextCompat.getColor(requireContext(), R.color.white)

            // Set selected for tvRatio
            binding.tvRatio.setTextColor(selectedColor)
            binding.tvRatio.compoundDrawablesRelative[0]?.mutate()?.setTint(selectedColor)

            // Reset others
            binding.tvAudio.setTextColor(defaultColor)
            binding.tvAudio.compoundDrawablesRelative[0]?.mutate()?.setTint(defaultColor)

            binding.tvResolution.setTextColor(defaultColor)
            binding.tvResolution.compoundDrawablesRelative[0]?.mutate()?.setTint(defaultColor)
        }

        binding.tvAudio.setOnClickListener {
            // Show audio options and hide others
            binding.audioOption.clAudioOptions.visibility = View.VISIBLE
            binding.mergeOptionsMerge.clMergeOption.visibility = View.GONE
            binding.resolutionOptionMerge.clQualitySelection.visibility = View.GONE
            binding.ratioOptionsMerge.clRatioOptions.visibility = View.GONE

            // Update selected text and drawable tint
            val selectedColor = ContextCompat.getColor(requireContext(), R.color.appcolor)
            val defaultColor = ContextCompat.getColor(requireContext(), R.color.white)

            // Set selected for tvAudio
            binding.tvAudio.setTextColor(selectedColor)
            binding.tvAudio.compoundDrawablesRelative[0]?.setTint(selectedColor)


            binding.tvResolution.setTextColor(defaultColor)
            binding.tvResolution.compoundDrawablesRelative[0]?.setTint(defaultColor)

            binding.tvRatio.setTextColor(defaultColor)
            binding.tvRatio.compoundDrawablesRelative[0]?.setTint(defaultColor)
        }


        selectedVideos?.let { videos ->
            val adapter = VideoMergeAdapter(requireContext(),
                ArrayList(videos),
                onItemRemoved = { video -> },
                onItemClicked = { video ->
                    currentPlayingVideo = video
                    playSelectedVideo(video)
                },
                onVideoRemoved = { video ->
                    onVideoRemoved(video)
                }
            )
            binding.rvSelectedVideos.adapter = adapter

            if (videos.isNotEmpty()) {
                currentPlayingVideo = videos.first()
                playSelectedVideo(currentPlayingVideo!!)
            }
        }

        setupMergeOptions(selectedVideos!!)

        binding.ivSave.setOnClickListener {
            selectedVideos?.let { videos ->
                if (videos.size > 1) {
                    navigateToConvertProgressFragment(videos)
                } else {
                    Toast.makeText(requireContext(), "Add more videos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAspectRatioSelection()
        setupResolutionSelection()
        setupAudioRecyclerView()
        selectedVideos?.let { videos ->
            extractAudioFromVideos(videos)
        }

    }

    private fun haveSameOrientation(videos: List<Video>): Boolean {
        val orientations = mutableSetOf<Int>()
        val retriever = MediaMetadataRetriever()

        for (video in videos) {
            try {
                retriever.setDataSource(video.path)
                val rotation =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                        ?.toInt() ?: 0
                orientations.add(rotation)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        retriever.release()
        return orientations.size <= 1
    }

    private fun setupAudioRecyclerView() {
        audioAdapter =
            ExtractAudioAdapter(requireContext(), mutableListOf(),  // Start with empty mutable list
                onItemClicked = { audioFile ->
                    // Handle audio item click (play/pause)
                },
                onMoreClicked = { audioFile ->
                    // Handle more options click
                }
            )

        binding.audioOption.rvAudios.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = audioAdapter
        }
    }

    private fun extractAudioFromVideos(videos: List<Video>) {
        val outputDir = requireContext().getExternalFilesDir(null)?.absolutePath ?: return

        CoroutineScope(Dispatchers.IO).launch {
            videos.forEach { video ->
                try {
                    val audioFile = extractAudio(video, outputDir)
                    withContext(Dispatchers.Main) {
                        audioFile?.let {
                            audioAdapter.addAudioFile(it) // This will auto-select the new item
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AudioExtraction", "Error extracting audio from ${video.title}", e)
                }
            }
        }
    }

    private fun findAudioTrackIndex(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                return i
            }
        }
        return -1
    }

    @SuppressLint("WrongConstant")
    private fun extractAudio(video: Video, outputDir: String): AudioFile? {
        val outputPath = "$outputDir/${video.title}_audio.mp3"

        try {
            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(video.path)

            val audioTrackIndex = findAudioTrackIndex(mediaExtractor)
            if (audioTrackIndex == -1) return null

            val format = mediaExtractor.getTrackFormat(audioTrackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null

            val mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            mediaExtractor.selectTrack(audioTrackIndex)

            mediaMuxer.addTrack(format)
            mediaMuxer.start()

            val buffer = ByteBuffer.allocate(1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                val sampleSize = mediaExtractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.flags = mediaExtractor.sampleFlags
                bufferInfo.presentationTimeUs = mediaExtractor.sampleTime

                mediaMuxer.writeSampleData(0, buffer, bufferInfo)
                mediaExtractor.advance()
            }

            mediaMuxer.stop()
            mediaMuxer.release()
            mediaExtractor.release()

            // Get duration and size
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(outputPath)
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0
            val size = File(outputPath).length()
            retriever.release()

            return AudioFile(
                id = video.id,
                title = "Audio from ${video.title}",
                path = outputPath,
                duration = duration,
                size = size
            )
        } catch (e: Exception) {
            Log.e("AudioExtraction", "Error extracting audio", e)
            return null
        }
    }


    private fun onVideoRemoved(video: Video) {
        if (selectedVideos?.size == 1) {
            Toast.makeText(
                requireContext(),
                "At least one video must be selected.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        selectedVideos = selectedVideos?.filter { it != video }

        if (selectedVideos.isNullOrEmpty()) {
            releasePlayer()
            return
        }

        if (currentPlayingVideo == video) {
            val nextVideo = selectedVideos?.firstOrNull()
            nextVideo?.let {
                playSelectedVideo(it)
            }
        }
    }

    private fun setupAspectRatioSelection() {
        val ratioOptions = listOf(
            binding.ratioOptionsMerge.tvOriginalRatio to null, // null = original
            binding.ratioOptionsMerge.tv16Ratio9 to Pair(16, 9),
            binding.ratioOptionsMerge.tv9Ratio16 to Pair(9, 16),
            binding.ratioOptionsMerge.tv1Ratio1 to Pair(1, 1),
            binding.ratioOptionsMerge.tv5Ratio4 to Pair(5, 4),
            binding.ratioOptionsMerge.tv4Ratio5 to Pair(4, 5),
            binding.ratioOptionsMerge.tv4Ratio3 to Pair(4, 3),
            binding.ratioOptionsMerge.tv3Ratio4 to Pair(3, 4)
        )



        ratioOptions.forEach { (textView, ratio) ->
            textView.setOnClickListener {
                ratioOptions.forEach { (tv, _) ->
                    tv.setBackgroundResource(R.drawable.ic_unselected_ratio)
                }
                textView.setBackgroundResource(R.drawable.ic_selected_ratio)
                selectedAspectRatio = ratio
                Log.i("selectedAspectRatio", "setupAspectRatioSelection: $selectedAspectRatio")
                applyAspectRatio(ratio)
            }
        }
    }

    private fun setupResolutionSelection() {
        val defaultBg = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        val selectedBg = Color.parseColor("#FEDDE5")

        binding.resolutionOptionMerge.rbQualitySmall.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedResolution = "small"
                binding.resolutionOptionMerge.clSmallQuality.setBackgroundColor(selectedBg)
                binding.resolutionOptionMerge.clMediumQuality.setBackgroundColor(defaultBg)
                binding.resolutionOptionMerge.clLargeQuality.setBackgroundColor(defaultBg)

                binding.resolutionOptionMerge.ivDoneSmall.visibility = View.VISIBLE
                binding.resolutionOptionMerge.ivDoneMedium.visibility = View.GONE
                binding.resolutionOptionMerge.ivDoneLarge.visibility = View.GONE

                binding.resolutionOptionMerge.rbQualityMedium.isChecked = false
                binding.resolutionOptionMerge.rbQualityLarge.isChecked = false
            }
        }

        binding.resolutionOptionMerge.rbQualityMedium.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedResolution = "medium"
                binding.resolutionOptionMerge.clSmallQuality.setBackgroundColor(defaultBg)
                binding.resolutionOptionMerge.clMediumQuality.setBackgroundColor(selectedBg)
                binding.resolutionOptionMerge.clLargeQuality.setBackgroundColor(defaultBg)

                binding.resolutionOptionMerge.ivDoneSmall.visibility = View.GONE
                binding.resolutionOptionMerge.ivDoneMedium.visibility = View.VISIBLE
                binding.resolutionOptionMerge.ivDoneLarge.visibility = View.GONE

                binding.resolutionOptionMerge.rbQualitySmall.isChecked = false
                binding.resolutionOptionMerge.rbQualityLarge.isChecked = false
            }
        }

        binding.resolutionOptionMerge.rbQualityLarge.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedResolution = "large"
                binding.resolutionOptionMerge.clSmallQuality.setBackgroundColor(defaultBg)
                binding.resolutionOptionMerge.clMediumQuality.setBackgroundColor(defaultBg)
                binding.resolutionOptionMerge.clLargeQuality.setBackgroundColor(selectedBg)

                binding.resolutionOptionMerge.ivDoneSmall.visibility = View.GONE
                binding.resolutionOptionMerge.ivDoneMedium.visibility = View.GONE
                binding.resolutionOptionMerge.ivDoneLarge.visibility = View.VISIBLE

                binding.resolutionOptionMerge.rbQualitySmall.isChecked = false
                binding.resolutionOptionMerge.rbQualityMedium.isChecked = false
            }
        }
    }


    private fun applyAspectRatio(ratio: Pair<Int, Int>?) {
        val playerView = binding.playerView
        val layoutParams = playerView.layoutParams

        if (ratio == null) {
            // Original ratio: match parent or wrap content depending on your design
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            val (w, h) = ratio
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            layoutParams.width = screenWidth
            layoutParams.height = (screenWidth * h / w.toFloat()).toInt()
        }

        playerView.layoutParams = layoutParams
    }


    private fun navigateToConvertProgressFragment(videos: List<Video>) {
        releasePlayer()
        // Get selected audios from adapter
        val selectedAudios = audioAdapter.getSelectedItems().toTypedArray()

        val selectedMergeStyle =
            when (binding.mergeOptionsMerge.mergeOptions.checkedRadioButtonId) {
                R.id.rbSequentially -> "Sequentially"
                R.id.rbLeftRight -> "Horizontally"
                R.id.rbUpDown -> "Vertically"
                else -> "Sequentially" // default fallback
            }


        val action = MergeVideoFragmentDirections.actionMergevideoToMergeprogressfragment(
            videos.toTypedArray(),
            selectedAspectRatio.toString(),
            selectedResolution,
            selectedAudios,
            selectedMergeStyle
        )

        navController.navigate(action)
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            releasePlayer()
            val action = MergeVideoFragmentDirections.actionMergevideoToAllvideo()
            navController.navigate(action)
        }
    }

    private fun setupMergeOptions(selectedVideos: List<Video>) {
        mergebinding.mergeOptions.visibility = View.VISIBLE
        mergebinding.rbSequentially.isChecked = true

        val outputDir = requireContext().getExternalFilesDir(null)?.absolutePath
        if (outputDir != null) {
            val outputPaths = mutableListOf<String>()

            selectedVideos.forEach { video ->
                val outputPath = "$outputDir/${video.title}_output.mp4"
                outputPaths.add(outputPath)
            }

            player = ExoPlayer.Builder(requireContext()).build()
            binding.playerView.player = player

            if (selectedVideos.isNotEmpty()) {
                videoUtils.setupExoPlayer(
                    binding,
                    player!!,
                    selectedVideos,
                    "sequential",
                    selectedVideos.first().path
                )
            }
        }
    }

    private fun playSelectedVideo(video: Video) {
        currentPlayingVideo = video
        player?.let {
            updateExoPlayerWithNewVideo(binding, video.path)
        }
    }

    private fun releasePlayer() {
        player?.stop()
        player?.release()
        player = null
    }

    private fun updateExoPlayerWithNewVideo(
        binding: FragmentMergeVideoBinding,
        outputPath: String
    ) {
        var player = binding.playerView.player
        if (player == null) {
            player = ExoPlayer.Builder(binding.root.context).build()
            binding.playerView.player = player
        }

        val mediaItem = MediaItem.fromUri(outputPath)
        player.stop()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun onResume() {
        super.onResume()
        selectedVideos =
            arguments?.let { MergeVideoFragmentArgs.fromBundle(it).selectedVideos.toList() }
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)
        toolbar.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)
        toolbar.visibility = View.VISIBLE
    }
}