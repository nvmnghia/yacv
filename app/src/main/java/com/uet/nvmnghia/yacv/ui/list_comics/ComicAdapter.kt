package com.uet.nvmnghia.yacv.ui.list_comics

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
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.glide.TopCrop
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ComicAdapter(
    private val glide: RequestManager,
) : ListAdapter<ComicMini, ComicAdapter.ViewHolder>(DIFF_CALLBACK) {

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
            .inflate(R.layout.browse_folder_item_comic, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = getItem(position)

        holder.comicName.text = FileUtils.folderNameFromPathUri(comic.fileUri)

        CoroutineScope(Dispatchers.IO).launch {
            val parser = ComicParser(context, comic.fileUri)
            val coverRequest = parser.requestCover()

            withContext(Dispatchers.Main) {
                glide.load(coverRequest)
                    .transform(TopCrop())
                    .into(holder.comicCover)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val comicName: TextView   = view.findViewById(R.id.browse_folder_item_comic_name)
        val comicCover: ImageView = view.findViewById(R.id.browse_folder_item_comic_cover)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ComicMini> = object : DiffUtil.ItemCallback<ComicMini>() {
            override fun areItemsTheSame(old: ComicMini, new: ComicMini): Boolean {
                return old.fileUri == new.fileUri
            }

            override fun areContentsTheSame(old: ComicMini, new: ComicMini): Boolean {
                return old.fileUri == new.fileUri
            }
        }
    }

}