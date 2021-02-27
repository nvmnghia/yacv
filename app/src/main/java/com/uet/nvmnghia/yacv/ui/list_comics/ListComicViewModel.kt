package com.uet.nvmnghia.yacv.ui.list_comics

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.comic.ComicRepository
import com.uet.nvmnghia.yacv.model.search.Metadata
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import com.uet.nvmnghia.yacv.model.search.QuerySingleType
import kotlinx.coroutines.launch
import java.net.URI


class ListComicViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,    // Access Fragment/Activity args
    private val searchHandler: MetadataSearchHandler
): ViewModel() {

    lateinit var comics: LiveData<List<ComicMini>>
    lateinit var title: String

    fun setMetadata(metadata: Metadata) {
        title = metadata.getLabel()
        viewModelScope.launch {
            comics = searchHandler.searchComics(metadata)
        }
    }

}