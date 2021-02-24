package com.uet.nvmnghia.yacv.ui.search

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import com.uet.nvmnghia.yacv.model.search.Metadata
import kotlinx.coroutines.launch


class SearchViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,    // Access Fragment/Activity args
    application: Application,
    searchHandler: MetadataSearchHandler
) : ViewModel() {

    val query: String = savedStateHandle.get<String>(application.resources.getString(R.string.query))
        ?: throw IllegalArgumentException("Missing query when instantiating SearchFragment")

    lateinit var results: LiveData<List<Metadata>>
    init {
        viewModelScope.launch {
            results = Transformations.map(searchHandler.search(query, true)) { flattenResults(it) }
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