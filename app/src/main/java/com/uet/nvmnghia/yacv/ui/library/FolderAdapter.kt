package com.uet.nvmnghia.yacv.ui.library

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.glide.TopCrop
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FolderAdapter(
    private val glide: RequestManager,
    private val comicDao: ComicDao
) : ListAdapter<Folder, FolderAdapter.ViewHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }


    //================================================================================
    // Adapter functions
    //================================================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.library_item_folder, parent,
                false)    // Not attach to parent so that parent doesn't receive touch events

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = getItem(position)

        holder.folderName.text = folder.name

        CoroutineScope(Dispatchers.IO).launch {
            // TODO: try to make this in a single call
            val firstComic = comicDao.getFirstComicInFolder(folder.id)

            // TODO: #6: Handle missing file!
            val parser = ComicParser(context, firstComic.fileUri)
            val coverRequest = parser.requestCover()

            withContext(Dispatchers.Main) {
                glide.load(coverRequest)
                    .transform(TopCrop())
                    .into(holder.folderCover)
            }
        }
    }


    //================================================================================
    // ViewHolder
    //================================================================================

    // Note that RecyclerView.ViewHolder(view) is a method call,
    // furthermore, a constructor call
    // This line delegates the constructor to RecyclerView.ViewHolder(view)
    // i.e. a shorter syntax for super(view)
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val folderName: TextView = view.findViewById(R.id.library_item_folder_name)
        val folderCover: ImageView = view.findViewById(R.id.library_item_folder_cover)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Folder> = object : DiffUtil.ItemCallback<Folder>() {
            override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
                return oldItem.uri == newItem.uri
            }

            override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
                return oldItem.uri == newItem.uri
            }
        }
    }
}