package com.uet.nvmnghia.yacv.ui.search.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.ui.search.SearchResultsAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalStateException


/**
 * Fragment containing result detail.
 * The full results of the selected category is displayed.
 * Only categories without cover (viewType in ViewHolder is
 * [SearchResultsAdapter.VIEW_TYPE_METADATA]) are displayed.
 * The one with cover is handled separately.
 */
@AndroidEntryPoint
class SearchDetailFragment : Fragment() {

    val viewModel: SearchDetailViewModel by viewModels()

    val args: SearchDetailFragmentArgs by navArgs()

    private lateinit var recyclerView: RecyclerView
    private lateinit var resultsAdapter: SearchResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val query = args.query
            ?: throw IllegalArgumentException("Cannot get any query.")
        viewModel.setQuery(query)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.search_list_result)
        recyclerView.layoutManager = LinearLayoutManager(context)

        resultsAdapter = SearchResultsAdapter(clickListener)
        recyclerView.adapter = resultsAdapter

        viewModel.results.observe(viewLifecycleOwner, resultsAdapter::submitList)

        return view
    }

    private val clickListener = View.OnClickListener { view ->
        val position = recyclerView.getChildLayoutPosition(view)
        val item = resultsAdapter.publicGetItem(position)

        when (item.getType()) {
            ComicMini.METADATA_GROUP_ID -> throw IllegalStateException("Unexpected comic results.")
        }
    }

}