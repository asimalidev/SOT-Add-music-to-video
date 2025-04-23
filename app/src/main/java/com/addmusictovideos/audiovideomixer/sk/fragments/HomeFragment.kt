package com.addmusictovideos.audiovideomixer.sk.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentHomeBinding
import com.addmusictovideos.audiovideomixer.sk.model.SharedViewModel


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private var navController: NavController? = null
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        binding.clAddMusicToVideo.setOnClickListener {
            sharedViewModel.setActionSource("changemusic")
            val action = HomeFragmentDirections.actionNavHomeToNavAllVideos()
            navController?.navigate(action)
        }

        binding.btnCutAudio.setOnClickListener {
            sharedViewModel.setActionSource("cutaudio")
            val action = HomeFragmentDirections.actionNavHomeToNavAllAudios(null)
            navController?.navigate(action)
        }


        binding.extractAudioBtn.setOnClickListener {
            sharedViewModel.setActionSource("extractaudio")
            val action = HomeFragmentDirections.actionNavHomeToNavAllVideos()
            navController?.navigate(action)
        }

        binding.btnSlowMotion.setOnClickListener {
            sharedViewModel.setActionSource("slowvideo")
            val action = HomeFragmentDirections.actionNavHomeToNavAllVideos()
            navController?.navigate(action)
        }

        binding.btnFastMotion.setOnClickListener {
            sharedViewModel.setActionSource("fastvideo")
            val action = HomeFragmentDirections.actionNavHomeToNavAllVideos()
            navController?.navigate(action)
        }

        binding.btnCutVideo.setOnClickListener {
            sharedViewModel.setActionSource("trimvideo")
            val action = HomeFragmentDirections.actionNavHomeToNavAllVideos()
            navController?.navigate(action)
        }
        binding.MergeVideoBtn.setOnClickListener {
            sharedViewModel.setActionSource("mergevideo")
            val action = HomeFragmentDirections.actionNavHomeToNavAllVideos()
            navController?.navigate(action)
        }

        binding.MergeAudioBtn.setOnClickListener {
            sharedViewModel.setActionSource("mergeaudio")
            val action = HomeFragmentDirections.actionNavHomeToNavAllAudios(null)
            navController?.navigate(action)
        }

        binding.btnMyCreation.setOnClickListener {
            val action = HomeFragmentDirections.actionNavAllVideosToNavAlloutput()
            navController?.navigate(action)
        }

    }
}