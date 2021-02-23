package com.uet.nvmnghia.yacv.ui.search

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import com.uet.nvmnghia.yacv.model.search.SearchableMetadata
import kotlinx.coroutines.launch


class SearchViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,    // Access Fragment/Activity args
    application: Application,
    searchHandler: MetadataSearchHandler
) : ViewModel() {

    val query: String = savedStateHandle.get<String>(application.resources.getString(R.string.query))
        ?: throw IllegalArgumentException("Missing query when instantiating SearchFragment")

    lateinit var results: LiveData<List<List<SearchableMetadata>>>
    init {
        viewModelScope.launch {
            results = searchHandler.search(query, true)
        }
    }
}