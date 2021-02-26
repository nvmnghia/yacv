package com.uet.nvmnghia.yacv.ui.search.preview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.search.queryFromSeeMore
import com.uet.nvmnghia.yacv.ui.search.SearchResultsAdapter
import com.uet.nvmnghia.yacv.ui.search.SeeMorePlaceholder
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalArgumentException


/**
 * Fragment containing search result preview.
 * The results are displayed in ExpandableListView, grouped by their types.
 * Only the first 3 results in each group are displayed.
 */
@AndroidEntryPoint
class SearchPreviewFragment : Fragment() {

    val viewModel: SearchPreviewViewModel by viewModels()

    val args: SearchPreviewFragmentArgs by navArgs()    // NOT THE SAME AS savedStateHandle

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
            SeeMorePlaceholder.METADATA_GROUP_ID ->
                SearchPreviewFragmentDirections
                    .actionSearchPreviewFragmentToSearchDetailFragment(queryFromSeeMore(item as SeeMorePlaceholder))
                    .let { findNavController().navigate(it) }
        }
    }

}