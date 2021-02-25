package com.uet.nvmnghia.yacv.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.genre.Genre
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import com.uet.nvmnghia.yacv.model.search.Metadata
import com.uet.nvmnghia.yacv.model.series.Series


/**
 * Given a 2D list of search results via [submitListToFlatten], display it.
 * This adapter is used in [SearchFragment], which means it only display the preview.
 * The 2D list has at most 5 elements (5 result groups correspond to 5 search
 * categories), and can be empty.
 * Each result group is guarantee to have from 1 to
 * [MetadataSearchHandler.NUM_PREVIEW_MATCH] + 1 = 4 elements.
 * However, at most 3 results are displayed per category.
 * If the fourth result is included, "See more..." is displayed, but the result
 * itself is not shown.
 */
class SearchResultsAdapter(
    private val clickListener: View.OnClickListener
) : ListAdapter<Metadata, SearchResultsAdapter.ResultViewHolder>(DIFF_CALLBACK) {

    // ListAdapter manages the list, so no list here, and always use getItem()
//    // Flattened result list
//    private lateinit var flatPreviewResults: MutableList<SearchableMetadata>


    //================================================================================
    // Adapter override
    //================================================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val inflater = LayoutInflater.from(parent.context!!)
        val inflate: (Int) -> View = { layoutID -> inflater.inflate(layoutID, parent, false) }

        return when (viewType) {
            VIEW_TYPE_GROUP    -> inflate(R.layout.search_list_group)
                .apply { setOnClickListener(clickListener) }
                .let { ResultGroupViewHolder(it) }
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
            VIEW_TYPE_GROUP    -> (holder as ResultGroupViewHolder).setTitle(item.getLabel())
            VIEW_TYPE_COMIC    -> (holder as ComicResultViewHolder).setLabel(item)
            VIEW_TYPE_METADATA -> (holder as MetadataResultViewHolder).setLabel(item)
            VIEW_TYPE_SEEMORE  -> null
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item.getType()) {
            ResultGroupPlaceholder.METADATA_GROUP_ID -> VIEW_TYPE_GROUP
            ComicMini.METADATA_GROUP_ID              -> VIEW_TYPE_COMIC
            Series.METADATA_GROUP_ID, Folder.METADATA_GROUP_ID, Character.METADATA_GROUP_ID,
                    Author.METADATA_GROUP_ID, Genre.METADATA_GROUP_ID -> VIEW_TYPE_METADATA
            SeeMorePlaceholder.METADATA_GROUP_ID -> VIEW_TYPE_SEEMORE
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
     * View holder for group title
     */
    class ResultGroupViewHolder(groupView: View) : ResultViewHolder(groupView) {

        private var groupTitle: TextView = groupView.findViewById(R.id.search_list_group_title)

        fun setTitle(title: String) {
            groupTitle.text = title
        }

    }

    /**
     * View holder specific to comic results.
     */
    class ComicResultViewHolder(itemView: View) : LabelledResultViewHolder(itemView) {

        private var itemLabel: TextView = itemView.findViewById(R.id.search_list_item_comic_label)

        override fun setLabel(label: String) {
            itemLabel.text = label
        }

    }

    /**
     * ViewHolder for non-comic results.
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
        const val VIEW_TYPE_GROUP    = 1
        const val VIEW_TYPE_COMIC    = 2
        const val VIEW_TYPE_METADATA = 3
        const val VIEW_TYPE_SEEMORE  = 4

        val DIFF_CALLBACK: DiffUtil.ItemCallback<Metadata> = object : DiffUtil.ItemCallback<Metadata>() {
            override fun areItemsTheSame(old: Metadata, aNew: Metadata): Boolean {
                return old.getType() == aNew.getType() && old.getID() == aNew.getID()
            }

            override fun areContentsTheSame(old: Metadata, aNew: Metadata): Boolean {
                return old.getLabel() == aNew.getLabel()    // This method is called only if areItemsTheSame returns true
            }
        }
    }

}