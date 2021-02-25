package com.uet.nvmnghia.yacv.ui.search

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import com.uet.nvmnghia.yacv.model.search.Metadata
import com.uet.nvmnghia.yacv.model.search.QueryMultipleTypes
import com.uet.nvmnghia.yacv.model.search.QuerySingleType
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException


class SearchViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,    // Access Fragment/Activity args
    application: Application,
    private val searchHandler: MetadataSearchHandler
) : ViewModel() {

    lateinit var results: LiveData<List<Metadata>>
    lateinit var query: String

    fun triggerSearch(querySingleType: QuerySingleType?, queryMultipleTypes: QueryMultipleTypes?) {
        if (querySingleType == null && queryMultipleTypes == null) {
            throw IllegalArgumentException("SearchViewModel cannot get any query.")
        }

        viewModelScope.launch {
            results = when {
                querySingleType != null -> {
                    query = querySingleType.query
                    searchHandler.search(querySingleType)
                }
                queryMultipleTypes != null -> {
                    query = queryMultipleTypes.query
                    Transformations.map(searchHandler.search(queryMultipleTypes)) { flattenResults(it, query) }
                }
                else -> throw IllegalStateException("SearchViewModel queries are null although checked.")
            }
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
                    flattened[flattened.lastIndex] =
                        SeeMorePlaceholder(group[0], query)
                }
            }

            return flattened
        }
    }

}