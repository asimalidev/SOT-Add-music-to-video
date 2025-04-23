package com.addmusictovideos.audiovideomixer.sk.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentVideoOutputFoldersBinding
import com.addmusictovideos.audiovideomixer.sk.utils.FilePathManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class VideoOutputFoldersFragment : Fragment(), TabLayout.OnTabSelectedListener {
    companion object {
        const val OUTPUT_TYPE = "output_type"
        const val TYPE_VIDEO = "video"
        val TYPE_AUDIO = "audio"

        fun newInstance(type: String): VideoOutputFoldersFragment {
            val fragment = VideoOutputFoldersFragment()
            val args = Bundle()
            args.putString(OUTPUT_TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }

    val audioFolderNames = arrayOf(
        "Cut Audio",
        "Video To MP3"
    )
    private var _binding: FragmentVideoOutputFoldersBinding? = null
    private val binding get() = _binding!!

    private lateinit var folderPath: Array<String>
    private lateinit var tabNames: Array<String>
    private lateinit var audioFolderPath: Array<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoOutputFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back press handling
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = VideoOutputFoldersFragmentDirections.actionOutputfolderToHome()
                    findNavController().navigate(action)
                }
            })

        binding.ivBack.setOnClickListener {
            val action = VideoOutputFoldersFragmentDirections.actionOutputfolderToHome()
            findNavController().navigate(action)
        }

        val type = arguments?.getString(OUTPUT_TYPE) ?: TYPE_VIDEO

        if (type == TYPE_AUDIO) {
            tabNames = audioFolderNames
            audioFolderPath = arrayOf(
                FilePathManager(requireContext()).getTrimAudioParentDir(),
                FilePathManager(requireContext()).getMP3ParentDir()
            )
            folderPath = audioFolderPath
        } else {
            tabNames = arrayOf(
                "Cut Video", "Fast Video","Slow Video", "Change Music")
            folderPath = arrayOf(
                FilePathManager(requireContext()).getTrimmedVideoParentDir(),
                FilePathManager(requireContext()).getFastSpeedParentDir(),
                FilePathManager(requireContext()).getSlowSpeedParentDir(),
                FilePathManager(requireContext()).getChangeMusicVideoParentDir()
//                FilePathManager(requireContext()).getConvertedGifParentDir(),
//                FilePathManager(requireContext()).getCompressedParentDir(),
//                FilePathManager(requireContext()).getMergeVideoParentDir(),
//                FilePathManager(requireContext()).getTrimmedVideoParentDir(),
//                FilePathManager(requireContext()).getChangeSpeedParentDir(),
//                FilePathManager(requireContext()).getReverseVideoParentDir(),
//                FilePathManager(requireContext()).getConvertVideoFormatsParentDir(),
//                FilePathManager(requireContext()).getChangeMusicVideoParentDir(),
//                FilePathManager(requireContext()).getFilterVideoParentDir(),
//                FilePathManager(requireContext()).getSlideshowVideoParentDir()
            )
        }

        // Set up ViewPager
        binding.viewPager.adapter = object : FragmentStateAdapter(this@VideoOutputFoldersFragment) {
            override fun createFragment(position: Int): Fragment {
                return FileListsFragment.newInstance(position, type, folderPath[position])
            }

            override fun getItemCount(): Int = tabNames.size
        }

        // Attach TabLayoutMediator
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.tab_item_layout, null, false)
            val txtTitle = view.findViewById<TextView>(R.id.tab_title)
            txtTitle.text = tabNames[position]
            view.findViewById<LinearLayout>(R.id.tab_layout).setBackgroundResource(
                if (position == 0) R.drawable.tab_bg_selected else R.drawable.tab_bg
            )
            txtTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tab.customView = view
        }.attach()

        // Restore selected tab AFTER mediator is attached
        val preferences = requireActivity().getSharedPreferences("TabState", Context.MODE_PRIVATE)
        val savedTabPosition = preferences.getInt("selectedTabPosition", 0)
        binding.viewPager.post {
            binding.viewPager.setCurrentItem(savedTabPosition, false)
        }

        binding.tabs.addOnTabSelectedListener(this)
    }


    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.let {
            binding.viewPager.setCurrentItem(it.position, false) // Sync tab with ViewPager

            it.customView?.let { view ->
                view.findViewById<LinearLayout>(R.id.tab_layout)
                    .setBackgroundResource(R.drawable.tab_bg_selected)
                val txtTitle = view.findViewById<TextView>(R.id.tab_title)
                txtTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }


    override fun onTabUnselected(tab: TabLayout.Tab?) {
        tab?.customView?.let { view ->
            view.findViewById<LinearLayout>(R.id.tab_layout)
                .setBackgroundResource(R.drawable.tab_bg)
            val txtTitle = view.findViewById<TextView>(R.id.tab_title)
            txtTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        tab?.customView?.let { view ->
            view.findViewById<LinearLayout>(R.id.tab_layout)
                .setBackgroundResource(R.drawable.tab_bg_selected)
            val txtTitle = view.findViewById<TextView>(R.id.tab_title)
            txtTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        toggleUIVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        toggleUIVisibility(View.VISIBLE)
        val selectedTabPosition = binding.tabs.selectedTabPosition
        val preferences = requireActivity().getSharedPreferences("TabState", Context.MODE_PRIVATE)
        preferences.edit().putInt("selectedTabPosition", selectedTabPosition).apply()
    }

    private fun toggleUIVisibility(visibility: Int) {
        requireActivity().findViewById<ConstraintLayout>(R.id.clToolbar).visibility = visibility
        requireActivity().findViewById<TextView>(R.id.tvToolbar).text = getString(R.string.Video_Editor)

    }
}