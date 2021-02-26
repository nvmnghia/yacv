package com.uet.nvmnghia.yacv.ui.search.preview

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.uet.nvmnghia.yacv.model.search.*
import com.uet.nvmnghia.yacv.ui.search.ResultGroupPlaceholder
import com.uet.nvmnghia.yacv.ui.search.SeeMorePlaceholder
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


class SearchPreviewViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,    // Access Fragment/Activity args
    private val searchHandler: MetadataSearchHandler
) : ViewModel() {

    lateinit var results: LiveData<List<Metadata>>

    fun setQuery(query: QueryMultipleTypes) {
        savedStateHandle["query"] = query
        viewModelScope.launch {
            results = Transformations.map(searchHandler.search(query)) { flattenResults(it, query.query) }
        }
    }


    companion object {
        /**
         * Given a list of result groups, flatten it then submit.
         * [query] string is needed for [Metadata].
         * The flattened list includes:
         * - [ResultGroupPlaceholder] as the first item in a group
         * - All group's item
         * - [Metadata] if needed
         * - Repeat the above for all groups
         */
        private fun flattenResults(previewResults: List<List<Metadata>>, query: String): List<Metadata> {
            val flattened = mutableListOf<Metadata>()

            previewResults.forEach { group ->
                // Group title
                flattened.add(ResultGroupPlaceholder(group[0]))

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