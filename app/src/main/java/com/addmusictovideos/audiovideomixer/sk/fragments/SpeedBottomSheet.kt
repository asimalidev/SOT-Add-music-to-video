package com.addmusictovideos.audiovideomixer.sk.fragments

import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.SpeedLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SpeedBottomSheet(private val mediaPlayer: MediaPlayer?) : BottomSheetDialogFragment() {

    interface OnSpeedSelectedListener {
        fun onSpeedSelected(speed: Float)
    }

    private var _binding: SpeedLayoutBinding? = null
    private val binding get() = _binding!!
    private var listener: OnSpeedSelectedListener? = null
    private var selectedSpeed: Float = 1.0f // Default speed
    private var selectedPosition: Int = 2 // Default position (tvSpeed3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SpeedLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = targetFragment as? OnSpeedSelectedListener ?: activity as? OnSpeedSelectedListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivClose.setOnClickListener { dismiss() }

        // Load last saved selection
        loadSavedSpeedSelection()

        // Set up speed options
        setupSpeedSelection()

        // Done button to confirm speed selection
        binding.ivDone.setOnClickListener {
            listener?.onSpeedSelected(selectedSpeed)
            Log.e("select", "Speed in bottom is $selectedSpeed")
            dismiss()
        }
    }

    private fun setupSpeedSelection() {
        val speedOptions = listOf(
            binding.tvSpeed1 to 0.5f,
            binding.tvSpeed2 to 0.75f,
            binding.tvSpeed3 to 1.0f,
            binding.tvSpeed4 to 1.25f,
            binding.tvSpeed5 to 1.5f,
            binding.tvSpeed6 to 2.0f
        )

        // Restore last selected speed and position
        val selectedTextView = speedOptions.getOrNull(selectedPosition)?.first
        selectedTextView?.let { updateSpeed(it, selectedSpeed) }

        // Set click listeners for all speed options
        speedOptions.forEachIndexed { index, (view, speed) ->
            view.setOnClickListener {
                updateSpeed(view, speed)
                saveSpeedSelection(speed, index)
            }
        }
    }


    private fun updateSpeed(selectedView: TextView, speed: Float) {
        selectedSpeed = speed
        resetOtherSpeedBackgrounds(selectedView)
        selectedView.setBackgroundResource(R.drawable.rounded_bg) // Highlight selected

        Log.e("select", "Speed selected: $selectedSpeed")

        // Apply speed in real-time if MediaPlayer is available
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.playbackParams = it.playbackParams.setSpeed(selectedSpeed)
                } else {
                    it.playbackParams = it.playbackParams.setSpeed(selectedSpeed)
                    it.pause() // Ensure it retains speed when played
                }
            } catch (e: Exception) {
                Log.e("SpeedBottomSheet", "Error setting playback speed: ${e.message}")
            }
        }
    }


    private fun resetOtherSpeedBackgrounds(selectedView: View) {
        val allSpeedViews = listOf(
            binding.tvSpeed1, binding.tvSpeed2, binding.tvSpeed3,
            binding.tvSpeed4, binding.tvSpeed5, binding.tvSpeed6
        )
        for (view in allSpeedViews) {
            if (view != selectedView) {
                view.setBackgroundResource(R.drawable.rounded_bg_stroke) // Reset unselected state
            }
        }
    }

    private fun saveSpeedSelection(speed: Float, position: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putFloat("selectedSpeed", speed)
            .putInt("selectedSpeedPosition", position)
            .apply()
    }

    private fun loadSavedSpeedSelection() {
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        selectedSpeed = sharedPreferences.getFloat("selectedSpeed", 1.0f) // Default 1.0x
        selectedPosition = sharedPreferences.getInt("selectedSpeedPosition", 2) // Default position (tvSpeed3)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener { dialog ->
                val bottomSheetDialog = dialog as com.google.android.material.bottomsheet.BottomSheetDialog

            }
        }
    }
}