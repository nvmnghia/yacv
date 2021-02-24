package com.uet.nvmnghia.yacv.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.ui.list_comics.ListComicViewModel
import dagger.hilt.android.AndroidEntryPoint


/**
 * Fragment containing search result preview.
 * The results are displayed in ExpandableListView, grouped by their types.
 * Only the first 3 results in each group are displayed.
 */
@AndroidEntryPoint
class SearchFragment : Fragment() {

    val viewModel: SearchViewModel by viewModels()

    private lateinit var previewResultsAdapter: SearchPreviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

//        val textView: TextView = view.findViewById(R.id.search_test_textview)
//        viewModel.results.observe(viewLifecycleOwner) { nestedResults ->
//            textView.text = nestedResults.joinToString("\n") { results ->
//                results.joinToString("; ") { result -> result.getLabel() }
//            }
//        }

        val recyclerView: RecyclerView = view.findViewById(R.id.search_list_preview_result)
        recyclerView.layoutManager = LinearLayoutManager(context)

        previewResultsAdapter = SearchPreviewAdapter()
        recyclerView.adapter = previewResultsAdapter

        viewModel.results.observe(viewLifecycleOwner) { previewResults ->
            previewResultsAdapter.submitListToFlatten(previewResults)
        }

        return view
    }

}