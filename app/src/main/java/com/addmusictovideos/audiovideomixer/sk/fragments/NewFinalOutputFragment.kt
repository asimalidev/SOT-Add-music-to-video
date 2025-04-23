package com.addmusictovideos.audiovideomixer.sk.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentNewFinalOutputBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewFinalOutputFragment : Fragment() {
    private lateinit var binding: FragmentNewFinalOutputBinding
    private lateinit var outputPath: String
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewFinalOutputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = NewFinalOutputFragmentDirections.actionProgressToHome()
                    navController.navigate(action)
                }
            })

        outputPath = NewFinalOutputFragmentArgs.fromBundle(requireArguments()).videoPath
        displayOutputDetails()

        binding.ivHome.setOnClickListener {
            val action = NewFinalOutputFragmentDirections.actionProgressToHome()
            navController.navigate(action)
        }

        binding.ivBack.setOnClickListener {
            val action = NewFinalOutputFragmentDirections.actionProgressToHome()
            navController.navigate(action)
        }

        binding.tvEditName.setOnClickListener {
            showRenameDialog()
        }

        binding.llWhatsapp.setOnClickListener { shareVideo("com.whatsapp") }
        binding.llInstagram.setOnClickListener { shareVideo("com.instagram.android") }
        binding.llfacebook.setOnClickListener { shareVideo("com.facebook.katana") }
        binding.llInfo.setOnClickListener { showInfoDialog() }

    }

    private fun shareVideo(packageName: String) {
        val fileUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            File(outputPath)
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_TEXT, "Check out this video converted with MyApp!")
            `package` = packageName // Specific app package
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "App not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showInfoDialog() {
        val outputFile = File(outputPath)

        if (!outputFile.exists()) {
            Toast.makeText(requireContext(), "File not found!", Toast.LENGTH_SHORT).show()
            return
        }

        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.info_dialog, null)

        // Initialize dialog components
        val tvName = dialogView.findViewById<TextView>(R.id.tvfileName)
        val tvFilePath = dialogView.findViewById<TextView>(R.id.tvfilePath)
        val tvFileSize = dialogView.findViewById<TextView>(R.id.tvfileSize)
        val tvFileDate = dialogView.findViewById<TextView>(R.id.tvfileDate)
        val tvFileFormat = dialogView.findViewById<TextView>(R.id.tvfileFormat)
        val tvFileDuration = dialogView.findViewById<TextView>(R.id.tvfileDuration)
        val tvFileResolution = dialogView.findViewById<TextView>(R.id.tvfileResolution)
        val tvClose = dialogView.findViewById<ImageView>(R.id.tvClose)

        // Set values
        tvName.text = outputFile.name
        tvFilePath.text = outputFile.absolutePath
        tvFileSize.text = getFileSize(outputFile)
        tvFileDate.text = getCurrentDate()
        tvFileFormat.text = outputFile.extension.uppercase()

        // Get duration and resolution
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(outputPath)

        val durationMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                ?: 0L
        tvFileDuration.text = formatDuration(durationMs)

        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        tvFileResolution.text =
            if (width != null && height != null) "${width}x$height" else "Unknown"

        retriever.release()

        // Create and show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        tvClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd ", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun formatDuration(durationMs: Long): String {
        val minutes = (durationMs / 1000) / 60
        val seconds = (durationMs / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun displayOutputDetails() {
        val outputFile = File(outputPath)
        if (outputFile.exists()) {
            binding.outputFileName.text = outputFile.name
            binding.outputFileSize.text = getFileSize(outputFile)

            val fileExtension = outputFile.extension.lowercase()

            when {
                fileExtension in listOf("mp4", "mkv", "avi", "mov", "flv") -> {
                    binding.ivmusic.setImageResource(R.drawable.ic_video_image)
                    binding.tvToolbar.text = getString(R.string.video_saved)
                    binding.clItem.setOnClickListener {
                        val action = NewFinalOutputFragmentDirections.actionFinaloutputToPlayvideo(outputPath)
                        navController.navigate(action)
                    }
                }
                fileExtension in listOf("mp3", "aac", "wav", "m4a", "ogg") -> {
                    binding.ivmusic.setImageResource(R.drawable.ic_music)
                    binding.tvToolbar.text = getString(R.string.audio_saved)
                    binding.clItem.setOnClickListener {
                        val action = NewFinalOutputFragmentDirections.actionFinaloutputToPlayaudio(outputPath,"audio",outputPath)
                        navController.navigate(action)
                    }
                }

                else -> {
                    binding.ivmusic.setImageResource(R.drawable.ic_video_image)
                    binding.tvToolbar.text = getString(R.string.file_saved)
                }
            }
        } else {
            binding.outputFileName.text = getString(R.string.file_not_found)
            binding.outputFileSize.text = ""
        }
    }

    private fun showRenameDialog() {
        val context = requireContext()
        val outputFile = File(outputPath)

        if (!outputFile.exists()) {
            Toast.makeText(context, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        val editText = EditText(context).apply {
            setText(outputFile.nameWithoutExtension) // Set current name
            inputType = InputType.TYPE_CLASS_TEXT
        }

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.rename_file))
            .setView(editText)
            .setPositiveButton(getString(R.string.rename)) { _, _ ->
                val newName = editText.text.toString().trim()
                renameOutputFile(newName)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
            .also { dialog ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isAllCaps = false
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.isAllCaps = false
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    ?.setTextColor(ContextCompat.getColor(context, R.color.appcolor))
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    ?.setTextColor(ContextCompat.getColor(context, R.color.appcolor))
            }

    }

    private fun renameOutputFile(newName: String) {
        val outputFile = File(outputPath)

        if (newName.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a valid name", Toast.LENGTH_SHORT).show()
            return
        }

        val newFile = File(outputFile.parent, "$newName.mp4")

        if (newFile.exists()) {
            Toast.makeText(
                requireContext(),
                "File with this name already exists!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val renamed = outputFile.renameTo(newFile)
        if (renamed) {
            outputPath = newFile.absolutePath
            binding.outputFileName.text = newFile.name
            Toast.makeText(requireContext(), "File renamed successfully!", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Failed to rename file!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileSize(file: File): String {
        val sizeInKB = file.length() / 1024
        return if (sizeInKB >= 1024) {
            "%.2f MB".format(sizeInKB / 1024.0)
        } else {
            "$sizeInKB KB"
        }
    }

    override fun onResume() {
        super.onResume()
        toggleUIVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        toggleUIVisibility(View.VISIBLE)
    }

    private fun toggleUIVisibility(visibility: Int) {
        requireActivity().findViewById<ConstraintLayout>(R.id.clToolbar).visibility = visibility
    }
}