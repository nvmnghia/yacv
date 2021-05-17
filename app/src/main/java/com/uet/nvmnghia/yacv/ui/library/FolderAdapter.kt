package com.uet.nvmnghia.yacv.ui.library

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.covercache.CoverCache
import com.uet.nvmnghia.yacv.glide.TopCrop
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class FolderAdapter(
    private val glide: RequestManager,
    private val comicDao: ComicDao,
    private val coverCache: CoverCache
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

            // Off UI anyway
            val setup = glide.load(coverRequest)
                .transform(TopCrop())
//                .thumbnail(loadThumbnail(coverRequest))
                .listener(getGlideLoadListener(coverRequest, firstComic.id))

            withContext(Dispatchers.Main) {
                setup.into(holder.folderCover)      // Must be on UI
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


    //================================================================================
    // Misc
    //================================================================================

    private fun loadThumbnail(coverRequest: ComicParser.PageRequest) =
        glide.load(File("")).transform(TopCrop())

    /**
     * Get [RequestListener] to cache low res cover.
     */
    private fun getGlideLoadListener(coverRequest: ComicParser.PageRequest, comicId: Long) = object : RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?,
            target: Target<Drawable>?, isFirstResource: Boolean): Boolean = false

        override fun onResourceReady(resource: Drawable?, model: Any?,
            target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            cacheLowRes(coverRequest, comicId)
            return false
        }
    }

    /**
     * Cache low res cover.
     */
    private fun cacheLowRes(coverRequest: ComicParser.PageRequest, comicId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val glideCache = glide.downloadOnly()
                .load(coverRequest)
                .submit()
                .get()
            coverCache.cache(comicId, glideCache)
        }
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