package com.addmusictovideos.audiovideomixer.sk.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentSlowVideoBinding


class SlowVideoFragment : Fragment() {
    private lateinit var videoPath: String
    private lateinit var binding: FragmentSlowVideoBinding
    private var exoPlayer: ExoPlayer? = null
    private var originalDuration: Long = 0L
    private var isUpdatingSelection = false  // Prevents recursive calls


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSlowVideoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoPath = SlowVideoFragmentArgs.fromBundle(requireArguments()).videoPath
        originalDuration = getVideoDuration(videoPath)
        binding.tvOrignalDuration.text = formatDuration(originalDuration)

        binding.tvMute.text = if (binding.muteStatus.isChecked) getString(R.string.mute) else getString(R.string.unmute)

        binding.muteStatus.setOnCheckedChangeListener { _, isChecked ->
            binding.tvMute.text = if (isChecked) getString(R.string.mute)  else getString(R.string.unmute)
        }

        binding.ivBack.setOnClickListener {
            val action = SlowVideoFragmentDirections.actionNavSpeedToNavAllVideo()
            findNavController().navigate(action)
        }

        val selectedColor = Color.parseColor("#00FF19")
        val unselectedColor = Color.GRAY

        val radioGroups = listOf(binding.radioGroup1)

        val radioButtons = listOf(
            binding.rb025x, binding.rb05x, binding.rb075x, binding.rb1x
        )

        fun updateSelectedSpeed(checkedId: Int) {
            if (checkedId == -1) return  // Ignore invalid selections

            // Reset colors
            radioButtons.forEach { setRadioButtonColor(it, unselectedColor) }

            // Set selected button color
            val selectedRadioButton = view.findViewById<RadioButton>(checkedId)
            setRadioButtonColor(selectedRadioButton, selectedColor)

            val selectedSpeed = when (checkedId) {
                R.id.rb_025x -> 0.25f
                R.id.rb_05x -> 0.5f
                R.id.rb_075x -> 0.75f
                R.id.rb_1x -> 1.0f
                else -> 1.0f
            }

            // Show UI
            binding.ivConverstion.visibility = View.VISIBLE
            binding.tvNewDuration.visibility = View.VISIBLE

            // Update new duration
            val adjustedDuration = originalDuration / selectedSpeed
            binding.tvNewDuration.text = formatDuration(adjustedDuration.toLong())

            // Save button
            binding.ivSave.setOnClickListener {
                val muteState = binding.muteStatus.isChecked
                val action = SlowVideoFragmentDirections.actionNavSpeedToProgressSpeed(
                    videoPath, selectedSpeed, muteState
                )
                findNavController().navigate(action)
            }
        }

        // Define the listener before using it
        val radioGroupListener = object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                if (isUpdatingSelection || checkedId == -1) return

                isUpdatingSelection = true  // Prevent recursive calls

                // Uncheck all radio buttons in the other group
                radioGroups.forEach { otherGroup ->
                    if (otherGroup != group) {
                        otherGroup.setOnCheckedChangeListener(null)  // Temporarily remove listener
                        otherGroup.clearCheck()
                        otherGroup.setOnCheckedChangeListener(this)  // Reattach listener properly
                    }
                }

                updateSelectedSpeed(checkedId)
                isUpdatingSelection = false
            }
        }

        // Set listener for both radio groups
        radioGroups.forEach { it.setOnCheckedChangeListener(radioGroupListener) }
    }

    private fun getVideoDuration(path: String): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        return durationStr?.toLongOrNull() ?: 0L
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(durationInMillis: Long): String {
        val minutes = (durationInMillis / 1000) / 60
        val seconds = (durationInMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun setRadioButtonColor(radioButton: RadioButton, color: Int) {
        radioButton.buttonTintList = ColorStateList.valueOf(color)
    }

    override fun onResume() {
        super.onResume()
        initializePlayer(videoPath)
        toggleUIVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.release()
        exoPlayer = null
        toggleUIVisibility(View.VISIBLE)
    }

    private fun toggleUIVisibility(visibility: Int) {
        requireActivity().findViewById<ConstraintLayout>(R.id.clToolbar).visibility = visibility
    }

    private fun initializePlayer(videoPath: String) {
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = exoPlayer
        val mediaItem = MediaItem.fromUri(videoPath)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
    }
}