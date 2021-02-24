package com.uet.nvmnghia.yacv.ui.search

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.uet.nvmnghia.yacv.R
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
    searchHandler: MetadataSearchHandler
) : ViewModel() {

    val querySingleType = savedStateHandle.get<QuerySingleType>(application.resources.getString(R.string.query_single_type))
    val queryMultipleTypes = savedStateHandle.get<QueryMultipleTypes>(application.resources.getString(R.string.query_multiple_types))

    lateinit var results: LiveData<List<Metadata>>
    init {
        if (querySingleType == null && queryMultipleTypes == null) {
            throw IllegalArgumentException("SearchViewModel cannot get any query.")
        }

        viewModelScope.launch {
            results = when {
                querySingleType != null -> searchHandler.search(querySingleType)
                queryMultipleTypes != null -> Transformations.map(searchHandler.search(queryMultipleTypes)) { flattenResults(it) }
                else -> throw IllegalStateException("SearchViewModel queries are null although checked.")
            }
        }
    }


    companion object {
        /**
         * Given a list of result groups, flatten it then submit.
         * The flattened list includes:
         * - [ResultGroupPlaceholder] as the first item in a group
         * - All group's item
         * - [SeeMorePlaceholder] if needed
         * - Repeat the above for all groups
         */
        private fun flattenResults(previewResults: List<List<Metadata>>): List<Metadata> {
            val flattened = mutableListOf<Metadata>()

            previewResults.forEach { group ->
                // Group title
                flattened.add(ResultGroupPlaceholder(group[0]))

                // Group results
                flattened.addAll(group)

                // See More if needed
                if (group.size == MetadataSearchHandler.NUM_PREVIEW_MATCH + 1) {
                    flattened[flattened.lastIndex] = SeeMorePlaceholder(group[0])
                }
            }

            return flattened
        }
    }

}