package com.uet.nvmnghia.yacv.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.ui.list_comics.ListComicViewModel
import dagger.hilt.android.AndroidEntryPoint


/**
 * Fragment containing search result overview.
 */
@AndroidEntryPoint
class SearchFragment : Fragment() {

    val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val textView: TextView = view.findViewById(R.id.search_test_textview)
        textView.text = viewModel.query

        return view
    }

}