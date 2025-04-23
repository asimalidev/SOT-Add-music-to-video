package com.addmusictovideos.audiovideomixer.sk.fragments

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.addmusictovideos.audiovideomixer.sk.databinding.VolumeLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class VolumeBottomSheet(private val mediaPlayer: MediaPlayer?) : BottomSheetDialogFragment() {

    private var _binding: VolumeLayoutBinding? = null
    private val binding get() = _binding!!
    private var listener: OnVolumeSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VolumeLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    interface OnVolumeSelectedListener {
        fun onVolumeSelected(volume: Int)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Close button
        binding.ivClose.setOnClickListener { dismiss() }

        // Set default volume text
        binding.tvTotalVolume.text = "Volume: 100%"

        // Volume bar listener (Apply volume in real-time)
        binding.volumeBar.setOnVolumeChangeListener { volume ->
            binding.tvVolume.text = "$volume%"
            binding.tvTotalVolume.text = "Volume: $volume%"

            val normalizedVolume = volume / 500f
            mediaPlayer?.setVolume(normalizedVolume, normalizedVolume)
        }

        binding.ivDone.setOnClickListener {
            val selectedVolume = binding.tvVolume.text.toString().replace("%", "").toInt()

            // Save the selected volume to SharedPreferences
            val sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("selected_volume", selectedVolume)
            editor.apply()

            listener?.onVolumeSelected(selectedVolume)
            dismiss()
        }

        // Decrease volume
        binding.ivlowVolume.setOnClickListener {
            binding.volumeBar.updateVolume(-10) // Decrease by 10
        }

        // Increase volume
        binding.ivIncreaseVolume.setOnClickListener {
            binding.volumeBar.updateVolume(10) // Increase by 10
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = targetFragment as? OnVolumeSelectedListener
    }

}






