package com.uet.nvmnghia.yacv.ui.search.preview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.genre.Genre
import com.uet.nvmnghia.yacv.model.search.Metadata
import com.uet.nvmnghia.yacv.model.search.queryFromSeeMore
import com.uet.nvmnghia.yacv.model.series.Series
import com.uet.nvmnghia.yacv.ui.search.ResultGroupHeaderPlaceholder
import com.uet.nvmnghia.yacv.ui.search.SearchResultsAdapter
import com.uet.nvmnghia.yacv.ui.search.SeeMorePlaceholder
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException


/**
 * Fragment containing search result preview.
 * The results are displayed in ExpandableListView, grouped by their types.
 * Only the first 3 results in each group are displayed.
 * See [SearchResultsAdapter] comment for more information.
 */
@AndroidEntryPoint
class SearchPreviewFragment : Fragment() {

    val viewModel: SearchPreviewViewModel by viewModels()

    val args: SearchPreviewFragmentArgs by navArgs()    // NOT THE SAME AS savedStateHandle

    private lateinit var glide: RequestManager

    private lateinit var recyclerView: RecyclerView
    private lateinit var resultsAdapter: SearchResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val query = args.query
            ?: throw IllegalArgumentException("Cannot get any query.")
        viewModel.setQuery(query)

        glide = Glide.with(this)
            .setDefaultRequestOptions(
                RequestOptions.formatOf(DecodeFormat.PREFER_RGB_565)    // In this fragment, Glide only loads cover
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        viewModel.query.observe(viewLifecycleOwner)
            { title -> (requireActivity() as AppCompatActivity).supportActionBar?.title = "Results for: $title" }

        recyclerView = view.findViewById(R.id.search_list_result)
        recyclerView.layoutManager = LinearLayoutManager(context)

        resultsAdapter = SearchResultsAdapter(glide, clickListener)
        recyclerView.adapter = resultsAdapter

        viewModel.results.observe(viewLifecycleOwner, resultsAdapter::submitList)

        return view
    }

    private val clickListener = View.OnClickListener { view ->
        val position = recyclerView.getChildLayoutPosition(view)
        val item = resultsAdapter.publicGetItem(position)

        // @formatter:off
        when (item.getType()) {
            ResultGroupHeaderPlaceholder.METADATA_TYPE
                -> null
            ComicMini.METADATA_TYPE
                -> toReader(item as ComicMini)
            Series.METADATA_TYPE, Folder.METADATA_TYPE, Character.METADATA_TYPE, Author.METADATA_TYPE, Genre.METADATA_TYPE
                -> toListComic(item)
            SeeMorePlaceholder.METADATA_TYPE
                -> toSearchDetail(item as SeeMorePlaceholder)
            else -> throw IllegalStateException("Unexpected item type ${item.getType()}")
        }
        // @formatter:on
    }

    /**
     * Move to [com.uet.nvmnghia.yacv.ui.reader.ReaderFragment].
     */
    private fun toReader(comic: ComicMini) {
        val action = SearchPreviewFragmentDirections
            .actionSearchPreviewFragmentToReaderFragment(comic.getID())
        findNavController().navigate(action)
    }

    /**
     * Move to [com.uet.nvmnghia.yacv.ui.search.detail.SearchDetailFragment].
     */
    private fun toSearchDetail(seeMore: SeeMorePlaceholder) {
        val action = SearchPreviewFragmentDirections
            .actionSearchPreviewFragmentToSearchDetailFragment(queryFromSeeMore(seeMore))
        findNavController().navigate(action)
    }

    /**
     * Move to [com.uet.nvmnghia.yacv.ui.list_comics.ListComicFragment].
     */
    private fun toListComic(metadata: Metadata) {
        val action = SearchPreviewFragmentDirections
            .actionSearchPreviewFragmentToListComicFragment(metadata)
        findNavController().navigate(action)
    }

}