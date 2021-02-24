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
import com.uet.nvmnghia.yacv.model.genre.Genre
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import com.uet.nvmnghia.yacv.model.search.SearchableMetadata
import com.uet.nvmnghia.yacv.model.series.Series


/**
 * Given a 2D list of search results [flatPreviewResults] display it.
 * This adapter is used in SearchFragment, which means it only display the preview.
 * [flatPreviewResults] has at most 5 elements (5 search categories), and can be empty.
 * [flatPreviewResults]'s element (list of results of a category) is guarantee to have
 * from 1 to [MetadataSearchHandler.NUM_PREVIEW_MATCH] + 1 = 4 elements.
 * However, at most 3 results are displayed per category.
 * If the fourth result is included, "See more..." is displayed, but the result
 * itself is not shown.
 */
class SearchPreviewAdapter :
    ListAdapter<SearchableMetadata, SearchPreviewAdapter.ResultViewHolder>(DIFF_CALLBACK) {

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
            VIEW_TYPE_GROUP    -> ResultGroupViewHolder(inflate(R.layout.search_list_group))
            VIEW_TYPE_COMIC    -> ComicResultViewHolder(inflate(R.layout.search_list_item_comic))
            VIEW_TYPE_METADATA -> MetadataResultViewHolder(inflate(R.layout.search_list_item_metadata))
            VIEW_TYPE_SEEMORE  -> SeeMoreViewHolder(inflate(R.layout.search_list_item_see_more))
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
        return when (val item = getItem(position)) {
            SeeMorePlaceholder        -> VIEW_TYPE_SEEMORE
            is ResultGroupPlaceholder -> VIEW_TYPE_GROUP
            is ComicMini              -> VIEW_TYPE_COMIC
            is Series, is Character, is Author, is Genre -> VIEW_TYPE_METADATA
            else -> throw IllegalStateException("Unexpected metadata of type ${item::class}")
        }
    }

    /**
     * Given a list of result groups, flatten it then submit.
     * The flattened list includes:
     * - [ResultGroupViewHolder] as the first item in a group
     * - All group's item
     * - [SeeMorePlaceholder] if needed
     * - Repeat the above for all groups
     */
    fun submitListToFlatten(previewResults: List<List<SearchableMetadata>>) {
        val flattened = mutableListOf<SearchableMetadata>()

        previewResults.forEach { group ->
            // Group title
            flattened.add(ResultGroupPlaceholder(group[0]))

            // Group results
            flattened.addAll(group)

            // See More if needed
            if (group.size == MetadataSearchHandler.NUM_PREVIEW_MATCH + 1) {
                flattened[flattened.lastIndex] = SeeMorePlaceholder
            }
        }

        submitList(flattened)
    }


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

        fun setLabel(metadata: SearchableMetadata) {
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

    /**
     * Given a [childPosition], check if it's a See More item.
     */
    private fun isSeeMore(childPosition: Int): Boolean =
        childPosition == MetadataSearchHandler.NUM_PREVIEW_MATCH

    private fun throwIllegalViewType(viewType: Int): Nothing {
        throw IllegalArgumentException("Invalid viewType: $viewType. " +
                "Valid ones: $VIEW_TYPE_COMIC, $VIEW_TYPE_METADATA, $VIEW_TYPE_SEEMORE.")
    }

    companion object {
        // Item view types
        const val VIEW_TYPE_GROUP    = 1
        const val VIEW_TYPE_COMIC    = 2
        const val VIEW_TYPE_METADATA = 3
        const val VIEW_TYPE_SEEMORE  = 4

        val DIFF_CALLBACK: DiffUtil.ItemCallback<SearchableMetadata> = object : DiffUtil.ItemCallback<SearchableMetadata>() {
            override fun areItemsTheSame(old: SearchableMetadata, new: SearchableMetadata): Boolean {
                return old.getType() == new.getType() && old.getID() == new.getID()
            }

            override fun areContentsTheSame(old: SearchableMetadata, new: SearchableMetadata): Boolean {
                return old.getType() == new.getType() && old.getLabel() == new.getLabel()
            }
        }
    }

}