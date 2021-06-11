package com.uet.nvmnghia.yacv.ui.search.detail

import androidx.lifecycle.*
import com.uet.nvmnghia.yacv.model.search.Metadata
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import com.uet.nvmnghia.yacv.model.search.Query
import com.uet.nvmnghia.yacv.model.search.QuerySingleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,    // Access Fragment/Activity args
    private val searchHandler: MetadataSearchHandler
) : ViewModel() {

    lateinit var results: LiveData<List<Metadata>>

    val query: LiveData<String> = Transformations.map(
        savedStateHandle.getLiveData<Query>("query")) { query -> query.query }

    fun setQuery(query: QuerySingleType) {
        savedStateHandle["query"] = query
        viewModelScope.launch {
            results = searchHandler.search(query)
        }
    }

}