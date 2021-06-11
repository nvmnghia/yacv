package com.uet.nvmnghia.yacv.ui.search

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
import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.genre.Genre
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import com.uet.nvmnghia.yacv.model.search.Metadata
import com.uet.nvmnghia.yacv.model.series.Series
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Given a 2D list of search results, display it.
 * This adapter is used in both [com.uet.nvmnghia.yacv.ui.search.preview.SearchPreviewFragment]
 * and [com.uet.nvmnghia.yacv.ui.search.detail.SearchDetailFragment].
 * The 2D list has at most 6 sublists (6 result groups correspond to 6 search categories/metadata types),
 * and can be empty.
 * Each result group is guarantee to have from 1 to [MetadataSearchHandler.NUM_PREVIEW_MATCH] + 1 = 4 elements.
 * However, at most 3 results are displayed per category.
 * If the fourth result is included, "See More..." is displayed instead.
 */
class SearchResultsAdapter(
    private val glide: RequestManager,
    private val clickListener: View.OnClickListener
) : ListAdapter<Metadata, SearchResultsAdapter.ResultViewHolder>(DIFF_CALLBACK) {

    // ListAdapter manages the list, so no list here, and always use getItem()
//    // Flattened result list
//    private lateinit var flatPreviewResults: MutableList<SearchableMetadata>


    private lateinit var context: Context

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }


    //================================================================================
    // Adapter override
    //================================================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val inflater = LayoutInflater.from(parent.context!!)
        val inflate: (Int) -> View = { layoutID -> inflater.inflate(layoutID, parent, false) }

        return when (viewType) {
            VIEW_TYPE_GROUP_HEADER    -> inflate(R.layout.search_list_group_header)
                .apply { setOnClickListener(clickListener) }
                .let { ResultGroupHeaderViewHolder(it) }
            VIEW_TYPE_COMIC    -> inflate(R.layout.search_list_item_comic)
                .apply { setOnClickListener(clickListener) }
                .let { ComicResultViewHolder(it) }
            VIEW_TYPE_METADATA -> inflate(R.layout.search_list_item_metadata)
                .apply { setOnClickListener(clickListener) }
                .let { MetadataResultViewHolder(it) }
            VIEW_TYPE_SEEMORE  -> inflate(R.layout.search_list_item_see_more)
                .apply { setOnClickListener(clickListener) }
                .let { SeeMoreViewHolder(it) }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val item = getItem(position)    // Don't access the list directly

        when (val viewType = holder.itemViewType) {
            VIEW_TYPE_GROUP_HEADER    -> (holder as ResultGroupHeaderViewHolder).setTitle(item.getLabel())
            VIEW_TYPE_COMIC    -> {
                (holder as ComicResultViewHolder).run {
                    setLabel(item.getLabel())
                    setCover(glide, context, (item as ComicMini).fileUri)
                }
            }
            VIEW_TYPE_METADATA -> (holder as MetadataResultViewHolder).setLabel(item)
            VIEW_TYPE_SEEMORE  -> null
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item.getType()) {
            ResultGroupHeaderPlaceholder.METADATA_TYPE -> VIEW_TYPE_GROUP_HEADER
            ComicMini.METADATA_TYPE              -> VIEW_TYPE_COMIC
            Series.METADATA_TYPE, Folder.METADATA_TYPE, Character.METADATA_TYPE,
                    Author.METADATA_TYPE, Genre.METADATA_TYPE -> VIEW_TYPE_METADATA
            SeeMorePlaceholder.METADATA_TYPE -> VIEW_TYPE_SEEMORE
            else -> throw IllegalStateException("Unexpected metadata of type ${item::class}")
        }
    }

    fun publicGetItem(position: Int): Metadata = super.getItem(position)


    //================================================================================
    // View Holders
    //================================================================================

    /**
     * Mother of all these view holders.
     */
    open class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * Wrapper for setLabel.
     */
    abstract class LabelledResultViewHolder(itemView: View) : ResultViewHolder(itemView) {

        abstract fun setLabel(label: String)

        fun setLabel(metadata: Metadata) {
            setLabel(metadata.getLabel())
        }

    }

    /**
     * View holder for group header.
     */
    class ResultGroupHeaderViewHolder(groupView: View) : ResultViewHolder(groupView) {

        private var headerTitle: TextView = groupView.findViewById(R.id.search_list_header_title)

        fun setTitle(title: String) {
            headerTitle.text = title
        }

    }

    /**
     * View holder specific to comic results.
     * Comic result has preview but metadata result doesn't.
     */
    class ComicResultViewHolder(itemView: View) : LabelledResultViewHolder(itemView) {

        private var itemLabel: TextView = itemView.findViewById(R.id.search_list_item_comic_label)
        private var itemCover: ImageView = itemView.findViewById(R.id.search_list_item_comic_cover)

        override fun setLabel(label: String) {
            itemLabel.text = label
        }

        fun setCover(glide: RequestManager, context: Context, fileUri: String) {
            CoroutineScope(Dispatchers.IO).launch {
                val parser = ComicParser(context, fileUri)
                val coverRequest = parser.requestCover()

                withContext(Dispatchers.Main) {
                    glide.load(coverRequest)
                        .transform(TopCrop())
                        .into(itemCover)
                }
            }
        }

    }

    /**
     * ViewHolder for non-comic results.
     * Comic result has preview but metadata result doesn't.
     */
    class MetadataResultViewHolder(itemView: View) : LabelledResultViewHolder(itemView) {

        private var itemLabel: TextView = itemView.findViewById(R.id.search_list_item_metadata_label)

        override fun setLabel(label: String) {
            itemLabel.text = label
        }

    }

    /**
     * View holder for See More.
     */
    class SeeMoreViewHolder(itemView: View) : ResultViewHolder(itemView)


    //================================================================================
    // Misc
    //================================================================================

    companion object {
        // Item view types
        const val VIEW_TYPE_GROUP_HEADER = 1
        const val VIEW_TYPE_COMIC        = 2
        const val VIEW_TYPE_METADATA     = 3
        const val VIEW_TYPE_SEEMORE      = 4

        val DIFF_CALLBACK: DiffUtil.ItemCallback<Metadata> = object : DiffUtil.ItemCallback<Metadata>() {
            override fun areItemsTheSame(old: Metadata, new: Metadata): Boolean {
                return old.getType() == new.getType() && old.getID() == new.getID()
            }

            override fun areContentsTheSame(old: Metadata, new: Metadata): Boolean {
                return old.getLabel() == new.getLabel()    // This method is called only if areItemsTheSame returns true
            }
        }
    }

}