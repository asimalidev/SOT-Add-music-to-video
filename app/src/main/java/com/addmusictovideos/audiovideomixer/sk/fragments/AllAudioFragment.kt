package com.addmusictovideos.audiovideomixer.sk.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.adapter.AudioAdapter
import com.addmusictovideos.audiovideomixer.sk.adapter.AudioFolderAdapter
import com.addmusictovideos.audiovideomixer.sk.adapter.SelectedAudioAdapter
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentAllAudioBinding
import com.addmusictovideos.audiovideomixer.sk.model.Audio
import com.addmusictovideos.audiovideomixer.sk.model.Folder
import com.addmusictovideos.audiovideomixer.sk.model.SharedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@SuppressLint("InlinedApi", "Recycle", "Range")
class AllAudioFragment : Fragment() {
    private lateinit var binding: FragmentAllAudioBinding
    private lateinit var audioAdapter: AudioAdapter
    private lateinit var folderAdapter: AudioFolderAdapter
    private lateinit var navController: NavController
    private lateinit var videoPath:String
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var selectedVideosAdapter: SelectedAudioAdapter
    private lateinit var audioList: ArrayList<Audio>
    private lateinit var folderList: ArrayList<Folder>
    private lateinit var folderVideoCount: HashMap<String, Int>
    private var sortValue: Int = 0
    private var sortOrder: Int = 0

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllAudioBinding.inflate(layoutInflater, container, false)
        navController = findNavController()
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        binding.ivBack.setOnClickListener {
            val action = AllAudioFragmentDirections.actionNavAllAudiosToNavHome()
            navController.navigate(action)
        }
        videoPath = AllAudioFragmentArgs.fromBundle(requireArguments()).videopath.toString()
        Log.e("videoPath","video path is $videoPath")

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = AllAudioFragmentDirections.actionNavAllAudiosToNavHome()
                    navController.navigate(action)
                }
            })

        binding.TotalFile.setOnClickListener {
            if (binding.rvSelectedVideos.visibility == View.VISIBLE) {
                // Hide the RecyclerView and change the drawable to ic_tap_to_view
                binding.rvSelectedVideos.visibility = View.GONE
                binding.ivDelete.visibility = View.GONE
                binding.tvTotalFile.visibility = View.GONE
                binding.TotalFile.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_tap_to_view,
                    0
                )
            } else {
                // Show the RecyclerView and change the drawable to ic_dragdown
                binding.rvSelectedVideos.visibility = View.VISIBLE
                binding.ivDelete.visibility = View.VISIBLE
                binding.tvTotalFile.visibility = View.VISIBLE
                binding.TotalFile.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_dragdown,
                    0
                )
            }
        }


        binding.progressBar.visibility = View.VISIBLE
        // Initialize the local folder list
        folderList = ArrayList()
        // Run the video loading process in a background thread
        CoroutineScope(Dispatchers.IO).launch {
            // Load videos and folder data in the background
            audioList = getAllAudios(requireContext())
            folderVideoCount =
                getAudioCountPerFolder(requireContext()) // Get audio count per folder
            folderList = getFolderList() // Populate folder list


            // Update the UI after loading is done (back on the main thread)
            withContext(Dispatchers.Main) {
                // Hide the ProgressBar after loading is done
                binding.progressBar.visibility = View.GONE
                audioAdapter = AudioAdapter(
                    requireContext(),
                    audioList,
                    sharedViewModel
                ) { selectedVideo, isSelected ->
                    if (isSelected) {
                        selectedVideosAdapter.addVideo(selectedVideo)

                        if (sharedViewModel.actionSource.value == "mixaudio" || sharedViewModel.actionSource.value == "audioconvert" ) {
                            if (selectedVideosAdapter.getSelectedCount() > 2) {
                                selectedVideosAdapter.removeVideo(selectedVideosAdapter.getSelectedVideos()[0])
                            }
                        }

                        if (selectedVideosAdapter.getSelectedCount() >= 1) {
                            binding.clSelectedvideo.visibility = View.VISIBLE
                            binding.ivDelete.visibility = View.GONE
                        }
                    } else {
                        selectedVideosAdapter.removeVideo(selectedVideo)
                        if (selectedVideosAdapter.getSelectedCount() == 0) {
                            binding.clSelectedvideo.visibility = View.GONE
                            binding.ivDelete.visibility = View.GONE
                        }
                    }
                    updateTotalFileCount()
                }


                selectedVideosAdapter = SelectedAudioAdapter(requireContext(), ArrayList(),
                    { removedVideo ->
                        audioAdapter.deselectAudio(removedVideo) // Ensure the removed video is deselected
                        updateTotalFileCount()
                    },
                    findNavController(),
                    sharedViewModel,
//                    { audio ->
//                        val action =
//                            AllAudioFragmentDirections.actionAllVideosFragmentToTrimAudioFragment(
//                                audio.path
//                            ) // Pass only the filePath
//                        findNavController().navigate(action)
//
//                    },
//
//                    { audio ->
//                        val action =
//                            AllAudioFragmentDirections.actionAllaudioToAudiospeed(
//                                audio.path
//                            ) // Pass only the filePath
//                        findNavController().navigate(action)
//
//                    },
//                    { audio ->
//                        val action =
//                            AllAudioFragmentDirections.actionAllaudioToSplitaudio(
//                                audio.path
//                            ) // Pass only the filePath
//                        findNavController().navigate(action)
//
//                    },
//                    { video ->
//                        val selectedVideos = selectedVideosAdapter.getSelectedVideos()
//                        val action = AllAudioFragmentDirections.actionAllaudioFragmentToConvertAudioFragment(selectedVideos.toTypedArray())
//                        findNavController().navigate(action)
//                    }
//                    ,
                    { audio ->
                        val action =
                            AllAudioFragmentDirections.actionNavAllaudioToNavChangeMusic(videoPath, audio.path) // Pass only the filePath
                        findNavController().navigate(action)

                    },
                            { audio ->
                        val action =
                            AllAudioFragmentDirections.actionNavAllaudioToNavCutAudio(
                                audio.path
                            ) // Pass only the filePath
                        findNavController().navigate(action)

                    }
                )


                // Folder adapter for videos
                folderAdapter = AudioFolderAdapter(
                    requireContext(),
                    folderList,
                    folderVideoCount,
                    true
                ) { folderName, isAudio ->
                    showMediaInFolder(folderName, isAudio)
                }


                binding.rvallaudio.layoutManager = LinearLayoutManager(requireContext())
                binding.rvallaudio.adapter = audioAdapter

                binding.rvAllFolder.layoutManager = LinearLayoutManager(requireContext())
                binding.rvAllFolder.adapter = folderAdapter

                binding.rvSelectedVideos.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
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
                    binding.rvallaudio.visibility = View.GONE
                }
                binding.tvAll.setOnClickListener {
                    binding.rvAllFolder.visibility = View.GONE
                    binding.selectedViewAllFolder.visibility = View.GONE
                    binding.selectedViewAllVideos.visibility = View.VISIBLE
                    binding.rvallaudio.visibility = View.VISIBLE
                }

                binding.tvSortedOrderName.setOnClickListener {
                    showSortDialog()
                }



                binding.ivNext.setOnClickListener {
                    val selectedVideos = selectedVideosAdapter.getSelectedVideos()
                    sharedViewModel.actionSource.value?.let { source ->
                        val action = when (source) {
                            "mergeaudio" -> {
                                if (selectedVideos.size > 1) {
                                    AllAudioFragmentDirections.actionAllVideosFragmentToMergeVideoFragment(
                                        selectedVideos.toTypedArray()
                                    )
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Add more audio",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@setOnClickListener
                                }
                            }

//                            "audioconvert" -> {
//                                if (selectedVideos.isNotEmpty()) {
//                                    AllAudioFragmentDirections.actionAllaudioFragmentToConvertAudioFragment(
//                                        selectedVideos.toTypedArray()
//                                    )
//                                } else {
//                                    Toast.makeText(
//                                        requireContext(),
//                                        "Select at least one audio",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    return@setOnClickListener
//                                }
//                            }
//
//
//                            "mixaudio" -> {
//                                if (selectedVideos.size == 2) {
//                                    AllAudioFragmentDirections.actionAllVideosFragmentToMixaudioFragment(
//                                        selectedVideos.toTypedArray()
//                                    )
//                                } else {
//                                    Toast.makeText(
//                                        requireContext(),
//                                        "Add exactly two audios to mix",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    return@setOnClickListener
//                                }
//                            }
//
                            else -> AllAudioFragmentDirections.actionAllVideosFragmentToMergeVideoFragment(
                                selectedVideos.toTypedArray()
                            )
                        }

                        navController.navigate(action)
                    }
                }

            }
        }

        return binding.root
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
        dialog.setPositiveButton("OK") { _, _ ->
            // Apply sorting based on the selected option
            applySorting(sortValue, sortOrder)
        }

        dialog.setNegativeButton("Cancel", null)
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
                sortOrderRadioButtons[0].text = "From new to old"
                sortOrderRadioButtons[1].text = "From old to new"
                sortOrderGroup.visibility = View.VISIBLE
            }

            1 -> { // Name
                sortOrderRadioButtons[0].text = "From A to Z"
                sortOrderRadioButtons[1].text = "From Z to A"
                sortOrderGroup.visibility = View.VISIBLE
            }

            2 -> { // Duration
                sortOrderRadioButtons[0].text = "From long to short"
                sortOrderRadioButtons[1].text = "From short to long"
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
                    audioList.sortedByDescending { File(it.path).lastModified() }
                } else {
                    // From old to new (descending order)
                    audioList.sortedBy { File(it.path).lastModified() }
                }
            }

            1 -> { // Sort by Name
                if (sortOrder == 0) {
                    // From A to Z (ascending order)
                    audioList.sortedBy { it.title }
                } else {
                    // From Z to A (descending order)
                    audioList.sortedByDescending { it.title }
                }
            }

            2 -> { // Sort by Duration
                if (sortOrder == 0) {
                    // From long to short (descending order)
                    audioList.sortedByDescending { it.duration }
                } else {
                    // From short to long (ascending order)
                    audioList.sortedBy { it.duration }
                }
            }

            else -> audioList
        }

        // Update the adapter's list with the sorted data
        audioAdapter.updateData(ArrayList(sortedVideos))
    }

    @SuppressLint("NewApi")
    fun getAllAudios(context: Context): ArrayList<Audio> {
        val audioList = ArrayList<Audio>()
        val tempFolderList = ArrayList<String>() // Temporary list to track unique folders
        val sortOrder = getSortOrder(sortValue) + " DESC"

        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29+
            arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME, // Available in API 29+
                MediaStore.Audio.Media.RELATIVE_PATH // Available in API 29+
            )
        } else {
            arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DISPLAY_NAME // Use DISPLAY_NAME instead of BUCKET_DISPLAY_NAME
            )
        }

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            while (it.moveToNext()) {
                val titleC = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown"
                val idC = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)) ?: "Unknown"
                val albumC = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) ?: "Unknown Album"
                val artistC = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
                val sizeC = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)) ?: "0"
                val pathC = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)) ?: "Unknown"
                val durationC = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))

                // Get folder name based on API level
                val folderC = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)) ?: "Unknown Folder"
                } else {
                    File(pathC).parentFile?.name ?: "Unknown Folder"
                }

                try {
                    val file = File(pathC)
                    val artUriC = Uri.fromFile(file)
                    val audio = Audio(
                        title = titleC,
                        id = idC,
                        album = albumC,
                        artist = artistC,
                        folderName = folderC,
                        duration = durationC,
                        size = sizeC,
                        path = pathC,
                        artUri = artUriC
                    )

                    if (file.exists()) {
                        audioList.add(audio)
                    }

                    // Ensure the folder is not duplicated
                    if (!tempFolderList.contains(folderC)) {
                        tempFolderList.add(folderC)
                        folderList.add(Folder(id = folderC, folderName = folderC)) // Use folder name as ID
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        Log.e("audiolist", "audio list is :${audioList}")
        return audioList
    }




    private fun getSortOrder(sortValue: Int): String {
        return when (sortValue) {
            0 -> MediaStore.Audio.Media.DATE_ADDED // Corrected from Video to Audio
            1 -> MediaStore.Audio.Media.TITLE
            2 -> MediaStore.Audio.Media.DURATION
            else -> MediaStore.Audio.Media.DATE_ADDED
        }
    }




    private fun getAudioCountPerFolder(context: Context): HashMap<String, Int> {
        val folderAudioCount = HashMap<String, Int>()

        val projection: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Audio.Media.RELATIVE_PATH // Available in API 29+
            )
        } else {
            arrayOf(
                MediaStore.Audio.Media.DATA // Use DATA for API < 29
            )
        }

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,  // No specific selection
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val folderName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH))
                        ?.split("/")?.getOrNull(1) ?: "Unknown"
                } else {
                    val filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    File(filePath).parentFile?.name ?: "Unknown"
                }

                folderAudioCount[folderName] = folderAudioCount.getOrDefault(folderName, 0) + 1
            }
        }

        return folderAudioCount
    }



    private fun getFolderList(): ArrayList<Folder> {
        return ArrayList(folderList)
    }


    private fun updateTotalFileCount() {
        binding.TotalFile.text = "Files: ${selectedVideosAdapter.itemCount}"
    }

    private fun showMediaInFolder(folderName: String, isAudioFolder: Boolean) {
        if (isAudioFolder) {
            val filteredAudios = audioList.filter { it.folderName == folderName }
            audioAdapter.updateList(ArrayList(filteredAudios))
            Log.d("AudioFolderClick", "Clicked folder: $folderName")
            Log.d("FilteredAudios", "Audios: ${filteredAudios.size}")
            // Switch to audio view
            binding.rvAllFolder.visibility = View.GONE
            binding.rvallaudio.visibility = View.VISIBLE
        } else {
            val filteredAudios = audioList.filter { it.folderName == folderName }
            audioAdapter.updateList(ArrayList(filteredAudios))


            // Switch to video view
            binding.rvAllFolder.visibility = View.GONE
            binding.rvallaudio.visibility = View.VISIBLE
        }
    }


    override fun onResume() {
        super.onResume()
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)

        toolbar.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        audioAdapter.stopAudioOnPause()
        val toolbar: ConstraintLayout = requireActivity().findViewById(R.id.clToolbar)

        toolbar.visibility = View.VISIBLE
    }
}