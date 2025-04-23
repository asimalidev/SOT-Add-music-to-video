package com.addmusictovideos.audiovideomixer.sk.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentAllOutputBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class AllOutputFragment : Fragment() {
    private lateinit var binding: FragmentAllOutputBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAllOutputBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = AllOutputFragmentDirections.actionNavAlloutputFragmentToHome()
                    findNavController().navigate(action)
                }
            })

        binding.ivBack.setOnClickListener {
            val action = AllOutputFragmentDirections.actionNavAlloutputFragmentToHome()
            findNavController().navigate(action)
        }

        binding.clVideoOutput.setOnClickListener {
            val action = AllOutputFragmentDirections.actionNavAlloutputFragmentToVideoOutputFragment("video")
            findNavController().navigate(action)
        }

        binding.clAudioOutput.setOnClickListener {
            val action = AllOutputFragmentDirections
                .actionNavAlloutputFragmentToAudioOutputFragment("audio")
            findNavController().navigate(action)
        }

    }

    override fun onPause() {
        super.onPause()
        toggleUIVisibility(View.VISIBLE)
    }

    override fun onResume() {
        super.onResume()
        toggleUIVisibility(View.GONE)
    }

    private fun toggleUIVisibility(visibility: Int) {
        requireActivity().findViewById<ConstraintLayout>(R.id.clToolbar).visibility = visibility

    }
}