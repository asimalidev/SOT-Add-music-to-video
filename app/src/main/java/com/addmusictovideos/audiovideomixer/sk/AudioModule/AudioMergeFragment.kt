package com.addmusictovideos.audiovideomixer.sk.AudioModule

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.adapter.MergeAudioAdapter
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentAudioMergeBinding
import com.addmusictovideos.audiovideomixer.sk.model.Audio
import com.addmusictovideos.audiovideomixer.sk.model.RecyclerHelper
import com.addmusictovideos.audiovideomixer.sk.model.SharedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class AudioMergeFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private var selectedAudio: List<Audio>? = null
    private lateinit var binding: FragmentAudioMergeBinding
    private lateinit var mergeAudioAdapter: MergeAudioAdapter
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudioMergeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        navController = findNavController()
        // Get the list of selected audio
        selectedAudio = arguments?.let {
           AudioMergeFragmentArgs.fromBundle(it).selectedVideos.toList()
        }

        binding.ivBack.setOnClickListener {
            val action=AudioMergeFragmentDirections.actionMergeAudioFragmentToAllAudios(null)
            navController.navigate(action)
        }

        Log.e("list", "list item is $selectedAudio")



        binding.ivMerge.setOnClickListener {
            selectedAudio = mergeAudioAdapter.audioList
            val action = AudioMergeFragmentDirections.actionMergeAudioFragmentToMergeAudioProgressFragment(selectedAudio!!.toTypedArray())
            navController.navigate(action)
        }


        selectedAudio?.let { audioList ->
            updateToolbarText(audioList.size)
            mergeAudioAdapter = MergeAudioAdapter(
                requireContext(),
                ArrayList(audioList),
                onAudioClick = { audio, isSelected -> },
                onListUpdated = { updatedList ->
                    selectedAudio = updatedList
                    updateToolbarText(updatedList.size) // ✅ Update toolbar text dynamically
                }
            )

            binding.rvMergeItem.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMergeItem.adapter = mergeAudioAdapter

            // 🔹 2. Create RecyclerHelper
            val touchHelper = RecyclerHelper(
                mergeAudioAdapter.audioList,
                mergeAudioAdapter
            ).setRecyclerItemDragEnabled(true)
                .setRecyclerItemSwipeEnabled(false)

            // 🔹 3. Create ItemTouchHelper BEFORE passing it to adapter
            val itemTouchHelper = ItemTouchHelper(touchHelper)
            itemTouchHelper.attachToRecyclerView(binding.rvMergeItem)

            // 🔹 4. Now, set itemTouchHelper in adapter
            mergeAudioAdapter.setItemTouchHelper(itemTouchHelper)
        }
    }


    //    // Function to update the toolbar text
    @SuppressLint("SetTextI18n")
    fun updateToolbarText(itemCount: Int) {
        binding.tvToolbar.text = getString(R.string.to_be_merged, itemCount, if (itemCount == 1) "file" else "files")
    }


    override fun onResume() {
        super.onResume()
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)

        toolbar.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        mergeAudioAdapter.stopAudioOnPause()
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)

        toolbar.visibility = View.VISIBLE
    }
}