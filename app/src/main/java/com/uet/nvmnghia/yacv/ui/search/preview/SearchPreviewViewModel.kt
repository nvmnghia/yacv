package com.uet.nvmnghia.yacv.ui.search.preview

import androidx.lifecycle.*
import com.uet.nvmnghia.yacv.model.search.Metadata
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import com.uet.nvmnghia.yacv.model.search.QueryMultipleTypes
import com.uet.nvmnghia.yacv.ui.search.ResultGroupHeaderPlaceholder
import com.uet.nvmnghia.yacv.ui.search.SeeMorePlaceholder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchPreviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,    // Access Fragment/Activity args
    private val searchHandler: MetadataSearchHandler
) : ViewModel() {

    lateinit var results: LiveData<List<Metadata>>
    val query: LiveData<String> = Transformations.map(
        savedStateHandle.getLiveData<QueryMultipleTypes>("query")) { query -> query.query }

    fun setQuery(query: QueryMultipleTypes) {
        savedStateHandle["query"] = query
        viewModelScope.launch {
            results = Transformations.map(searchHandler.search(query)) { flattenResults(it, query.query) }
        }
    }

    companion object {
        /**
         * Given a 2D list of results (grouped by metadata type), flatten it.
         * [query] string is needed for [Metadata].
         * The flattened list includes:
         * - [ResultGroupHeaderPlaceholder] as the first item in a group
         * - All group's item
         * - [Metadata] if needed
         * - Repeat the above for all groups
         */
        private fun flattenResults(previewResults: List<List<Metadata>>, query: String): List<Metadata> {
            val flattened = mutableListOf<Metadata>()

            previewResults.forEach { group ->
                // Group title
                flattened.add(ResultGroupHeaderPlaceholder(group[0]))

                // Group results
                flattened.addAll(group)

                // See More if needed
                if (group.size == MetadataSearchHandler.NUM_PREVIEW_MATCH + 1) {
                    flattened[flattened.lastIndex] = SeeMorePlaceholder(group[0], query)
                }
            }

            return flattened
        }
    }

}