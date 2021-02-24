package com.uet.nvmnghia.yacv.ui.search.preview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uet.nvmnghia.yacv.R
import dagger.hilt.android.AndroidEntryPoint


/**
 * Fragment containing search result preview.
 * The results are displayed in ExpandableListView, grouped by their types.
 * Only the first 3 results in each group are displayed.
 */
@AndroidEntryPoint
class SearchPreviewFragment : Fragment() {

    val viewModel: SearchPreviewViewModel by viewModels()

    private lateinit var previewResultsAdapter: SearchPreviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_preview, container, false)

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