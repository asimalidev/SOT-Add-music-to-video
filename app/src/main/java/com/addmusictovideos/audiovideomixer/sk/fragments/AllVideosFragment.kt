package com.addmusictovideos.audiovideomixer.sk.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.CompoundButtonCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentAllVideosBinding
import com.addmusictovideos.audiovideomixer.sk.model.Folder
import com.addmusictovideos.audiovideomixer.sk.model.SharedViewModel
import com.addmusictovideos.audiovideomixer.sk.model.Video
import com.addmusictovideos.audiovideomixer.sk.adapter.SelectedVideosAdapter
import com.addmusictovideos.audiovideomixer.sk.adapter.VideoAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Suppress("IMPLICIT_CAST_TO_ANY")
@SuppressLint("InlinedApi", "Recycle", "Range")
class AllVideosFragment : Fragment() {
    private lateinit var binding: FragmentAllVideosBinding
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var navController: NavController
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var selectedVideosAdapter: SelectedVideosAdapter
    private lateinit var videoList: ArrayList<Video>
    private lateinit var folderList: ArrayList<Folder>
    private lateinit var folderVideoCount: HashMap<String, Int>
    private var sortValue: Int = 0
    private var sortOrder: Int = 0

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllVideosBinding.inflate(layoutInflater, container, false)
        navController = findNavController()
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        binding.ivBack.setOnClickListener {
            val action = AllVideosFragmentDirections.actionNavAllVideosToNavHome()
            navController.navigate(action)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = AllVideosFragmentDirections.actionNavAllVideosToNavHome()
                    navController.navigate(action)
                }
            })

        // Show the ProgressBar while loading videos
        binding.progressBar.visibility = View.VISIBLE
        // Initialize the local folder list
        folderList = ArrayList()
        // Run the video loading process in a background thread
        CoroutineScope(Dispatchers.IO).launch {
            // Load videos and folder data in the background
            videoList = getAllVideos(requireContext(), sharedViewModel.actionSource.value)
            folderVideoCount =
                getVideoCountPerFolder(requireContext()) // Get video count per folder
            folderList = getFolderList()

            // Update the UI after loading is done (back on the main thread)
            withContext(Dispatchers.Main) {
                // Hide the ProgressBar after loading is done
                binding.progressBar.visibility = View.GONE

                // Initialize the VideoAdapter and other adapters after the data is loaded
                videoAdapter =
                    VideoAdapter(requireContext(), videoList) { selectedVideo, isSelected ->
                        if (isSelected) {
                            selectedVideosAdapter.addVideo(selectedVideo)
                            if (selectedVideosAdapter.getSelectedCount() == 1) {
                                binding.rvSelectedVideos.visibility = View.VISIBLE
                                binding.ivDelete.visibility = View.VISIBLE
                            }
                        } else {
                            selectedVideosAdapter.removeVideo(selectedVideo)
                            if (selectedVideosAdapter.getSelectedCount() == 0) {
                                binding.rvSelectedVideos.visibility = View.GONE
                                binding.ivDelete.visibility = View.GONE
                            }
                        }
                        updateTotalFileCount()
                    }

                selectedVideosAdapter = SelectedVideosAdapter(
                    requireContext(),
                    ArrayList(),
                    { removedVideo ->
                        videoAdapter.deselectVideo(removedVideo) // Ensure the removed video is deselected
                        updateTotalFileCount()
                    },
                    findNavController(),
                    sharedViewModel,
                    { video ->
                        val action = AllVideosFragmentDirections.actionAllVideosFragmentToAllaudio(
                            video.path,
                            video.path
                        )
                        findNavController().navigate(action)
                    },
                    { video ->
                        val action = AllVideosFragmentDirections
                            .actionAllVideosFragmentToExtractAudio(video.path)
                        findNavController().navigate(action)

                    },
                    { video ->
                        val action =
                            AllVideosFragmentDirections.actionAllVideosFragmentToFastVideo(video.path)
                        findNavController().navigate(action)

                    },
                    { video ->
                        val action =
                            AllVideosFragmentDirections.actionAllVideosFragmentToSlowVideo(video.path)
                        findNavController().navigate(action)

                    },
                    { video ->
                        val action =
                            AllVideosFragmentDirections.actionNavAllvideoToNavCutVideo(
                                video.path
                            )
                        findNavController().navigate(action)

                    }
                )

                folderAdapter = FolderAdapter(
                    requireContext(),
                    folderList,
                    folderVideoCount,
                    false
                ) { folderName, isAudio ->
                    showVideosInFolder(folderName, isAudio)
                }

                binding.rvAllVideos.layoutManager = GridLayoutManager(requireContext(), 3)
                binding.rvAllVideos.adapter = videoAdapter

                binding.rvAllFolder.layoutManager = LinearLayoutManager(requireContext())
                binding.rvAllFolder.adapter = folderAdapter

                binding.rvSelectedVideos.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.rvSelectedVideos.adapter = selectedVideosAdapter

                // Delete all selected videos when clicking ivDelete
                binding.ivDelete.setOnClickListener {
                    selectedVideosAdapter.removeAllVideos()
                    updateTotalFileCount()
                }

                binding.tvFolders.setOnClickListener {
                    binding.rvAllFolder.visibility = View.VISIBLE
                    binding.selectedViewAllFolder.visibility = View.VISIBLE
                    binding.selectedViewAllVideos.visibility = View.GONE
                    binding.rvAllVideos.visibility = View.GONE
                }

                binding.tvAll.setOnClickListener {
                    binding.rvAllFolder.visibility = View.GONE
                    binding.selectedViewAllFolder.visibility = View.GONE
                    binding.selectedViewAllVideos.visibility = View.VISIBLE
                    binding.rvAllVideos.visibility = View.VISIBLE

                    val selectedVideos =
                        selectedVideosAdapter.getSelectedVideos() // Get the currently selected videos
                    videoAdapter.loadAllVideos(
                        videoList,
                        selectedVideos
                    ) // Load all videos and pass selected videos
                }



                binding.tvSortedOrderName.setOnClickListener {
                    showSortDialog()
                }

                val selectedVideos = selectedVideosAdapter.getSelectedVideos()

                binding.ivNext.setOnClickListener {
                    sharedViewModel.actionSource.value?.let { source ->
                        val videoPaths = selectedVideos.map { it.path }.toTypedArray()

                        try {
                            val action = when (source) {
                                "mergevideo" -> {
                                    if (selectedVideos.size > 1) {
                                        AllVideosFragmentDirections.actionAllVideosFragmentToMergeVideoFragment(
                                            selectedVideos.toTypedArray()
                                        )

                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "Add more videos",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@setOnClickListener
                                    }
                                }



                                else -> {
                                    // Handle unknown source or provide default navigation
                                    Toast.makeText(
                                        requireContext(),
                                        "Unknown action",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@setOnClickListener
                                }
                            }

                            // Use findNavController() consistently
                            findNavController().navigate(action)
                        } catch (e: IllegalArgumentException) {
                            Log.e("NavigationError", "Failed to navigate: ${e.message}")
                            Toast.makeText(
                                requireContext(),
                                "Navigation error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } ?: run {
                        Toast.makeText(
                            requireContext(),
                            "No action specified",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }




            }
        }

        return binding.root
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

    private fun showSortDialog() {
        val dialog = AlertDialog.Builder(requireContext())

        // Inflate the custom layout for the dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_sort, null)

        // Get references to the RadioButtons
        val radioDate = dialogView.findViewById<AppCompatRadioButton>(R.id.radioDate)
        val radioName = dialogView.findViewById<AppCompatRadioButton>(R.id.radioName)
        val radioDuration = dialogView.findViewById<AppCompatRadioButton>(R.id.radioDuration)

        // Get references to the RadioGroup for the sort order (Ascending/Descending)
        val sortOrderGroup = dialogView.findViewById<RadioGroup>(R.id.sortOrderGroup)
        val sortOrderRadioButtons = arrayOf(
            dialogView.findViewById<AppCompatRadioButton>(R.id.radioAsc),
            dialogView.findViewById<AppCompatRadioButton>(R.id.radioDesc)
        )

        // Define the ColorStateList for button tint
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked), // Unchecked state
                intArrayOf(android.R.attr.state_checked)   // Checked state
            ),
            intArrayOf(
                Color.DKGRAY, // Dark Gray for unchecked state
                Color.rgb(242, 81, 112) // Custom Red for checked state
            )
        )

        // Apply the button tint to all RadioButtons
        val radioButtons = arrayOf(radioDate, radioName, radioDuration) + sortOrderRadioButtons
        radioButtons.forEach { radioButton ->
            CompoundButtonCompat.setButtonTintList(radioButton, colorStateList)
        }

        // Set the initial checked state for sorting options
        when (sortValue) {
            0 -> radioDate.isChecked = true
            1 -> radioName.isChecked = true
            2 -> radioDuration.isChecked = true
        }

        // Set the initial checked state for sort order
        if (sortOrder == 0) {
            sortOrderRadioButtons[0].isChecked = true
        } else {
            sortOrderRadioButtons[1].isChecked = true
        }

        // Update the sort order options initially
        updateSortOrderOptions(sortValue, sortOrderGroup, sortOrderRadioButtons)

        // Set click listeners for sorting options
        radioButtons.forEach { radioButton ->
            radioButton.setOnClickListener {
                // Update selected sort option
                sortValue = when (it) {
                    radioDate -> 0
                    radioName -> 1
                    radioDuration -> 2
                    else -> sortValue
                }

                // Update sort order options
                updateSortOrderOptions(sortValue, sortOrderGroup, sortOrderRadioButtons)
            }
        }

        // **Fix: Listen for sort order changes and update sorting dynamically**
        sortOrderGroup.setOnCheckedChangeListener { _, checkedId ->
            sortOrder = if (checkedId == R.id.radioAsc) 0 else 1
        }

        // Set the positive button actions
        dialog.setPositiveButton(getString(R.string.ok)) { _, _ ->
            // Apply sorting based on the selected option
            applySorting(sortValue, sortOrder)
        }

        dialog.setNegativeButton(getString(R.string.cancel), null)
        dialog.setView(dialogView) // Set the custom layout view
        dialog.show()
    }

    private fun updateSortOrderOptions(
        sortOption: Int,
        sortOrderGroup: RadioGroup,
        sortOrderRadioButtons: Array<AppCompatRadioButton>
    ) {
        // Based on the sort option, update the visibility of the sort order options (ascending/descending)
        when (sortOption) {
            0 -> { // Date
                sortOrderRadioButtons[0].text = getString(R.string.from_new_to_old)
                sortOrderRadioButtons[1].text = getString(R.string.from_old_to_new)
                sortOrderGroup.visibility = View.VISIBLE
            }

            1 -> { // Name
                sortOrderRadioButtons[0].text = getString(R.string.from_a_to_z)
                sortOrderRadioButtons[1].text = getString(R.string.from_z_to_a)
                sortOrderGroup.visibility = View.VISIBLE
            }

            2 -> { // Duration
                sortOrderRadioButtons[0].text = getString(R.string.from_long_to_short)
                sortOrderRadioButtons[1].text = getString(R.string.from_short_to_long)
                sortOrderGroup.visibility = View.VISIBLE
            }

            else -> {
                sortOrderGroup.visibility = View.GONE // Hide sort order if no selection
            }
        }
    }

    private fun applySorting(sortValue: Int, sortOrder: Int) {
        // Get the current list of videos
        val sortedVideos = when (sortValue) {
            0 -> { // Sort by Date
                if (sortOrder == 0) {
                    // From new to old (ascending order)
                    videoList.sortedByDescending { File(it.path).lastModified() }
                } else {
                    // From old to new (descending order)
                    videoList.sortedBy { File(it.path).lastModified() }
                }
            }

            1 -> { // Sort by Name
                if (sortOrder == 0) {
                    // From A to Z (ascending order)
                    videoList.sortedBy { it.title }
                } else {
                    // From Z to A (descending order)
                    videoList.sortedByDescending { it.title }
                }
            }

            2 -> { // Sort by Duration
                if (sortOrder == 0) {
                    // From long to short (descending order)
                    videoList.sortedByDescending { it.duration }
                } else {
                    // From short to long (ascending order)
                    videoList.sortedBy { it.duration }
                }
            }

            else -> videoList
        }

        // Update the adapter's list with the sorted data
        videoAdapter.updateData(ArrayList(sortedVideos))
    }

    @SuppressLint("NewApi")
    fun getAllVideos(context: Context, actionSource: String?): ArrayList<Video> {
        val tempList = ArrayList<Video>()
        val tempFolderList = ArrayList<String>()

        val maxDurationGif = 30000L // 30 seconds in milliseconds
        val maxDurationCutVideo = 180000L // 3 minutes in milliseconds
        val maxDurationReverseVideo = 90000L // 1.5 minutes (90,000 milliseconds)

        val (selection, selectionArgs) = when (actionSource) {
            "VideotoGif" -> Pair(
                "${MediaStore.Video.Media.DURATION} <= ?",
                arrayOf(maxDurationGif.toString())
            )

            "cutvideo" -> Pair(
                "${MediaStore.Video.Media.DURATION} <= ?",
                arrayOf(maxDurationCutVideo.toString())
            )

            "ReverseVideo" -> Pair(
                "${MediaStore.Video.Media.DURATION} <= ?",
                arrayOf(maxDurationReverseVideo.toString())
            )

            else -> Pair(null, null)
        }

        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID
        )

        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            getSortOrder(sortValue)
        )

        cursor?.use {
            while (it.moveToNext()) {
                val titleC =
                    it.getString(it.getColumnIndex(MediaStore.Video.Media.TITLE)) ?: "Unknown"
                val idC = it.getString(it.getColumnIndex(MediaStore.Video.Media._ID)) ?: "Unknown"
                val folderC =
                    it.getString(it.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                        ?: "Internal Storage"
                val folderIdC =
                    it.getString(it.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)) ?: "Unknown"
                val sizeC = it.getString(it.getColumnIndex(MediaStore.Video.Media.SIZE)) ?: "0"
                val pathC =
                    it.getString(it.getColumnIndex(MediaStore.Video.Media.DATA)) ?: "Unknown"
                val durationC = it.getLong(it.getColumnIndex(MediaStore.Video.Media.DURATION))

                try {
                    val file = File(pathC)
                    val artUriC = Uri.fromFile(file)
                    val video = Video(
                        title = titleC,
                        id = idC,
                        folderName = folderC,
                        duration = durationC,
                        size = sizeC,
                        path = pathC,
                        artUri = artUriC
                    )

                    if (file.exists()) {
                        tempList.add(video)
                    }
                    if (!tempFolderList.contains(folderC) && !folderC.contains("Internal Storage")) {
                        tempFolderList.add(folderC)
                        folderList.add(Folder(id = folderIdC, folderName = folderC))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return tempList
    }


    private fun getSortOrder(sortValue: Int): String {
        return when (sortValue) {
            0 -> MediaStore.Video.Media.DATE_ADDED // Date
            1 -> MediaStore.Video.Media.TITLE // Name
            2 -> MediaStore.Video.Media.DURATION // Duration
            else -> MediaStore.Video.Media.DATE_ADDED // Default to Date
        }
    }

    private fun getVideoCountPerFolder(context: Context): HashMap<String, Int> {
        val folderVideoCount = HashMap<String, Int>()

        val projection = arrayOf(
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME // This provides the folder name
        )

        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,  // No specific selection
            null,
            null
        )

        cursor?.use {
            val folderColumnIndex =
                it.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            while (it.moveToNext()) {
                val folderName = it.getString(folderColumnIndex) ?: "Unknown"
                folderVideoCount[folderName] = folderVideoCount.getOrDefault(folderName, 0) + 1
            }
        }

        return folderVideoCount
    }

    private fun getFolderList(): ArrayList<Folder> {
        return ArrayList(folderList)
    }

    private fun updateTotalFileCount() {
        binding.tvTotalFile.text = "Total Files: ${selectedVideosAdapter.itemCount}"
    }


    private fun showVideosInFolder(folderName: String, isAudioFolder: Boolean) {
        val selectedVideos =
            selectedVideosAdapter.getSelectedVideos() // Get currently selected videos

        if (isAudioFolder) {
            val filteredAudios = videoList.filter { it.folderName == folderName }
            videoAdapter.updateList(ArrayList(filteredAudios))

            // Retain selected videos in the filtered list
            val selectedVideosInFolder = filteredAudios.filter { selectedVideos.contains(it) }
            videoAdapter.loadAllVideos(ArrayList(filteredAudios), ArrayList(selectedVideosInFolder))

            // Switch to audio view
            binding.rvAllFolder.visibility = View.GONE
            binding.rvAllVideos.visibility = View.VISIBLE
        } else {
            val filteredVideos = videoList.filter { it.folderName == folderName }
            videoAdapter.updateList(ArrayList(filteredVideos))

            // Retain selected videos in the filtered list
            val selectedVideosInFolder = filteredVideos.filter { selectedVideos.contains(it) }
            videoAdapter.loadAllVideos(ArrayList(filteredVideos), ArrayList(selectedVideosInFolder))

            // Switch to video view
            binding.rvAllFolder.visibility = View.GONE
            binding.rvAllVideos.visibility = View.VISIBLE
        }
    }


    override fun onResume() {
        super.onResume()
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)
        toolbar.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)
        toolbar.visibility = View.VISIBLE
    }
}