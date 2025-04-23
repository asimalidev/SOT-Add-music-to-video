package com.addmusictovideos.audiovideomixer.sk.fragments

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentCutAudioForAddMusicBinding
import com.addmusictovideos.audiovideomixer.sk.utils.AudioTrimmerView
import com.addmusictovideos.audiovideomixer.sk.utils.SharedViewModelForAddMusic
import com.arthenica.ffmpegkit.FFprobeKit
import java.util.concurrent.TimeUnit

class CutAudioForAddMusicFragment : Fragment(), SpeedBottomSheet.OnSpeedSelectedListener,
    VolumeBottomSheet.OnVolumeSelectedListener {
    private lateinit var binding: FragmentCutAudioForAddMusicBinding
    private lateinit var audioPath: String
    private lateinit var videoPath: String
    private var audioDurationMs: Long = 0L
    private var mediaPlayer: MediaPlayer? = null
    private var isplaying = false
    private lateinit var outputPath: String
    private var startTrimMs: Long = 0L
    private var endTrimMs: Long = 0L
    private var selectedSpeed: Float = 1.0f
    private var selectedVolume: Int = 100
    private var previousStartRatio: Float = 0f
    private val forwardTime = 5000
    private val backwardTime = 5000
    private lateinit var sharedViewModel: SharedViewModelForAddMusic


    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    val positionMs = player.currentPosition.toFloat()

                    val startMs = (binding.audioTrimmer.getTrimStart() * audioDurationMs)
                    val endMs = (binding.audioTrimmer.getTrimEnd() * audioDurationMs)

                    val currentRatio = if (positionMs in startMs..endMs) {
                        (positionMs - startMs) / (endMs - startMs)
                    } else {
                        0f // Reset if outside bounds
                    }

                    binding.audioTrimmer.updatePlaybackIndicator(currentRatio)
                    handler.postDelayed(this, 100)
                } else {
                    // Stop the handler when playback completes
                    handler.removeCallbacks(this)
                }
            }
        }
    }

    override fun onSpeedSelected(speed: Float) {
        selectedSpeed = speed

        val speedText = when (speed) {
            0.5f -> "0.5x"
            0.75f -> "0.75x"
            1.0f -> "1.0x"
            1.25f -> "1.25x"
            1.5f -> "1.5x"
            2.0f -> "2.0x"
            else -> "1.0x"
        }
        binding.tvSpeeds.text = speedText

        // Apply speed directly to the MediaPlayer
        mediaPlayer?.let {
            it.playbackParams = it.playbackParams.setSpeed(speed)
        }

        Log.d("TrimAudioFragment", "Speed selected: $selectedSpeed")

        // Save the selected speed to SharedPreferences
        val sharedPreferences =
            requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putFloat("selectedSpeed", selectedSpeed).apply()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCutAudioForAddMusicBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioPath = CutAudioForAddMusicFragmentArgs.fromBundle(requireArguments()).audioPath
        videoPath = CutAudioForAddMusicFragmentArgs.fromBundle(requireArguments()).videoPath
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModelForAddMusic::class.java]

        val sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        selectedSpeed = sharedPreferences.getFloat("selectedSpeed", 1.0f)

        binding.tvVolume.setOnClickListener {
            val bottomSheet = VolumeBottomSheet(mediaPlayer)
            bottomSheet.show(parentFragmentManager, "VolumeBottomSheet")
        }

        binding.ivBack.setOnClickListener {
            val action = CutAudioForAddMusicFragmentDirections.actionNavCutaudioToChangemusic(videoPath,audioPath)
            findNavController().navigate(action)
        }

        binding.tvSpeed.setOnClickListener {
            val bottomSheet = SpeedBottomSheet(mediaPlayer)
            bottomSheet.show(parentFragmentManager, "SpeedBottomSheet")
        }

        // Load waveform and duration
        loadWaveform(audioPath)
        setAudioDuration(audioPath)

        // Button click listeners for moving thumbs
        binding.startthumbforward.setOnClickListener { moveLeftThumb(-10) }
        binding.endthumbbackward.setOnClickListener { moveLeftThumb(10) }
        binding.endthumbforward.setOnClickListener { moveRightThumb(-10) }
        binding.endthumbRight.setOnClickListener { moveRightThumb(10) }

        binding.btnPlayPause.setOnClickListener { playPauseAudio() }
        binding.btnRewind.setOnClickListener { rewindAudio() }
        binding.btnForward.setOnClickListener { forwardAudio() }
        binding.btnStart.setOnClickListener { playFromStart() } // Play from start trim position
        binding.btnEnd.setOnClickListener { playFromEnd() } // Play from end trim position


        binding.audioTrimmer.setTrimChangeListener(object : AudioTrimmerView.TrimChangeListener {


            override fun onTrimChanged(startRatio: Float, endRatio: Float) {
                updateTrimTimes(startRatio, endRatio)

                // Seek only if the left thumb (startRatio) has changed
                if (isplaying && startRatio != previousStartRatio) {
                    mediaPlayer?.seekTo((startRatio * audioDurationMs).toInt())
                }

                // Update the previous start ratio
                previousStartRatio = startRatio
            }


            override fun onPlaybackSeeked(positionRatio: Float) {
                val seekPosition = (binding.audioTrimmer.getTrimStart() * audioDurationMs) +
                        (positionRatio * (binding.audioTrimmer.getTrimEnd() - binding.audioTrimmer.getTrimStart()) * audioDurationMs)
                mediaPlayer?.seekTo(seekPosition.toInt())
            }
        })

        binding.btnSaveVideo.setOnClickListener {
            val startText = binding.tvStartTime.text.toString()
            val endText = binding.tvEndTime.text.toString()

            val startTimeMillis = parseTimeToMilliseconds(startText)
            val endTimeMillis = parseTimeToMilliseconds(endText)

            startTrimMs = startTimeMillis
            endTrimMs = endTimeMillis

            val action = CutAudioForAddMusicFragmentDirections.actionTrimaudioToNavTrimaudioprogress(
                videoPath,
                audioPath,
                selectedVolume / 100f, // Directly send the current volume, not from SharedPreferences
                startTrimMs,
                endTrimMs,
                selectedSpeed
            )
            findNavController().navigate(action)
        }
    }


    override fun onVolumeSelected(volume: Int) {
        selectedVolume = volume
        binding.tvVolume.text = "$volume%" // Update UI with selected volume

        // Apply volume to MediaPlayer
        mediaPlayer?.setVolume(volume / 100f, volume / 100f)
    }


    private fun parseTimeToMilliseconds(timeString: String): Long {
        val timeParts = timeString.split(":")

        val minutes = timeParts[0].toIntOrNull() ?: 0
        val seconds = timeParts[1].toFloatOrNull() ?: 0f

        return (((minutes * 60) + seconds) * 1000).toLong()
    }



    private fun playPauseAudio() {
        if (mediaPlayer == null) setupMediaPlayer()

        mediaPlayer?.let {
            if (isplaying) {
                it.pause()
                handler.removeCallbacks(updateRunnable) // Stop updating indicator
            } else {
                if (it.currentPosition < startTrimMs.toInt() || it.currentPosition > endTrimMs.toInt()) {
                    it.seekTo(startTrimMs.toInt()); // Ensure playback starts from left trim
                }
                it.start()
                handler.post(updateRunnable) // Start updating indicator
            }
            isplaying = !isplaying
            binding.btnPlayPause.setBackgroundResource(if (isplaying) R.drawable.ic_pause else R.drawable.ic_playpause)
        }
    }




    private fun forwardAudio() {
        mediaPlayer?.let {
            val newPosition =
                (it.currentPosition + 5000).coerceAtMost((binding.audioTrimmer.trimEnd * audioDurationMs).toInt())
            Log.d(
                "TrimAudioFragment",
                "Forward: currentPos=${it.currentPosition}, newPosition=$newPosition"
            )
            it.seekTo(newPosition)
        }
    }

    private fun rewindAudio() {
        mediaPlayer?.let {
            val newPosition =
                (it.currentPosition - 5000).coerceAtLeast((binding.audioTrimmer.trimStart * audioDurationMs).toInt())
            Log.d(
                "TrimAudioFragment",
                "Rewind: currentPos=${it.currentPosition}, newPosition=$newPosition"
            )
            it.seekTo(newPosition)
        }
    }

    private fun playFromStart() {
        if (mediaPlayer == null) setupMediaPlayer()

        mediaPlayer?.let {
            it.seekTo(startTrimMs.toInt()) // Move to start trim position
            it.start()
            isplaying = true
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_pause)

            it.setOnCompletionListener {
                isplaying = false
                binding.btnPlayPause.setBackgroundResource(R.drawable.ic_playpause)
            }
        }
    }


    private fun playFromEnd() {
        if (mediaPlayer == null) setupMediaPlayer()

        // Ensure endTrimMs is updated before using it
        endTrimMs = (binding.audioTrimmer.trimEnd * audioDurationMs).toInt().toLong()

        Log.d(
            "TrimAudioFragment",
            "Trying to play from end. Updated endTrimMs: $endTrimMs, audioDurationMs: ${mediaPlayer?.duration}"
        )

        mediaPlayer?.let {
            if (endTrimMs <= 0 || endTrimMs > it.duration) {
                Log.e(
                    "TrimAudioFragment",
                    "Invalid endTrimMs: $endTrimMs (duration: ${it.duration})"
                )
                return
            }

            val offsetMs = 2000
            val seekPosition = (endTrimMs - offsetMs).coerceAtLeast(startTrimMs)
            Log.d("TrimAudioFragment", "Seeking to: $seekPosition")

            it.seekTo(seekPosition.toInt())

            it.setOnSeekCompleteListener { mp ->
                Log.d("TrimAudioFragment", "Seek completed, starting playback from $seekPosition")
                mp.start()
                isplaying = true
                binding.btnPlayPause.setBackgroundResource(R.drawable.ic_pause)

                handler.post(updateRunnable)

                mp.setOnCompletionListener { player ->
                    Log.d(
                        "TrimAudioFragment",
                        "Playback finished at ${player.currentPosition}, stopping at $endTrimMs"
                    )
                    if (player.currentPosition >= endTrimMs.toInt()) {
                        isplaying = false
                        binding.btnPlayPause.setBackgroundResource(R.drawable.ic_playpause)
                        handler.removeCallbacks(updateRunnable)
                    }
                }
            }
        }
    }


    private fun setupMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(requireContext(), Uri.parse(audioPath))
                prepare()

                // Retrieve saved volume level
                val sharedPreferences =
                    requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                selectedVolume =
                    sharedPreferences.getInt("selected_volume", 100) // Default to 100 if not found

                // Apply speed and volume settings
                playbackParams = playbackParams.setSpeed(selectedSpeed)
                setVolume(
                    selectedVolume / 100f,
                    selectedVolume / 100f
                ) // Convert to 0.0 - 1.0 range

                setOnCompletionListener {
                    isplaying = false
                    binding.btnPlayPause.setBackgroundResource(R.drawable.ic_playpause)
                    handler.removeCallbacks(updateRunnable)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun setAudioDuration(audioPath: String) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(requireContext(), Uri.parse(audioPath))
            audioDurationMs =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L

            val formattedDuration = formatTime(audioDurationMs)
            binding.tvTotalDuration.text = formattedDuration
            binding.tvStartTime.text = "00:00"
            binding.tvEndTime.text = formattedDuration
        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvTotalDuration.text = "00:00"
        } finally {
            retriever.release()
        }
    }

    private fun loadWaveform(audioPath: String) {
        // Example: Generate fake waveform data for now
        val waveform = FloatArray(100) { Math.random().toFloat() }
        binding.audioTrimmer.setWaveformData(waveform)
    }

    private fun moveLeftThumb(offset: Int) {
        binding.audioTrimmer.moveLeftThumbBy(offset.toFloat())

        val startRatio = binding.audioTrimmer.getTrimStart()
        val endRatio = binding.audioTrimmer.getTrimEnd()

        updateTrimTimes(startRatio, endRatio)
    }

    private fun moveRightThumb(offset: Int) {
        binding.audioTrimmer.moveRightThumbBy(offset.toFloat())

        val startRatio = binding.audioTrimmer.getTrimStart()
        val endRatio = binding.audioTrimmer.getTrimEnd()

        updateTrimTimes(startRatio, endRatio)
    }

    private fun updateTrimTimes(startRatio: Float, endRatio: Float) {
        val startTrim = (startRatio * audioDurationMs).toLong()
        val endTrim = (endRatio * audioDurationMs).toLong()
        val trimmedDuration = endTrim - startTrim

        binding.tvStartTime.text = formatTime(startTrim)
        binding.tvEndTime.text = formatTime(endTrim)
        binding.tvTotalDuration.text = formatTime(trimmedDuration) // Update total duration
    }

    private fun formatTime(milliseconds: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onPause()
        toggleUIVisibility(View.VISIBLE)
    }

    override fun onResume() {
        super.onResume()

        // Retrieve the saved volume
        val sharedPreferences =
            requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        selectedVolume = sharedPreferences.getInt("selected_volume", 100)
        Log.e("selectedvolume", " value of selected volume is $selectedVolume")

        // Apply saved speed and volume
        mediaPlayer?.let {
            it.playbackParams = it.playbackParams.setSpeed(selectedSpeed)
            it.setVolume(selectedVolume / 100f, selectedVolume / 100f)
        }

        binding.tvSpeeds.text = when (selectedSpeed) {
            0.5f -> "0.5x"
            0.75f -> "0.75x"
            1.0f -> "1.0x"
            1.25f -> "1.25x"
            1.5f -> "1.5x"
            2.0f -> "2.0x"
            else -> "1.0x"
        }

        toggleUIVisibility(View.GONE)
    }

    private fun toggleUIVisibility(visibility: Int) {
        requireActivity().findViewById<ConstraintLayout>(R.id.clToolbar).visibility = visibility

    }
}