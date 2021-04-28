package com.uet.nvmnghia.yacv.ui.list_comics

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.search.Metadata
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ListComicViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,    // Access Fragment/Activity args
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