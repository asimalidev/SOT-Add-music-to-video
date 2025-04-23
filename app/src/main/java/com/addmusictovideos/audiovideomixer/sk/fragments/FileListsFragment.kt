package com.addmusictovideos.audiovideomixer.sk.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.adapter.NewFileListAdapter
import com.addmusictovideos.audiovideomixer.sk.utils.PlaceholderContent
import java.io.File

class FileListsFragment : Fragment() {
    private lateinit var navController: NavController
    private var position = 1
    private var type = "video"
    private var folder = "folder"
    private val fileItems = ArrayList<PlaceholderContent.PlaceholderItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(POSITION_ARG)
            type = it.getString(TYPE_ARG).toString()
            folder = it.getString(FOLDER_PATH).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewFiles)
        val imageEmpty = view.findViewById<ImageView>(R.id.imageEmpty)

        // Setup RecyclerView once
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = NewFileListAdapter(fileItems, navController)
        recyclerView.adapter = adapter

        loadFiles(recyclerView, imageEmpty)
    }

    private fun loadFiles(recyclerView: RecyclerView, imageEmpty: ImageView) {
        val file = File(folder).listFiles()

        if (file == null || file.isEmpty()) {
            imageEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            imageEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            fileItems.clear()
            fileItems.addAll(file.map { f ->
                PlaceholderContent.PlaceholderItem(f.absolutePath, f.name)
            })
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    companion object {
        const val POSITION_ARG = "column-count"
        const val TYPE_ARG = "type"
        const val FOLDER_PATH = "folder_path"

        @JvmStatic
        fun newInstance(position: Int, type: String, path: String) =
            FileListsFragment().apply {
                arguments = Bundle().apply {
                    putInt(POSITION_ARG, position)
                    putString(TYPE_ARG, type)
                    putString(FOLDER_PATH, path)
                }
            }
    }
}