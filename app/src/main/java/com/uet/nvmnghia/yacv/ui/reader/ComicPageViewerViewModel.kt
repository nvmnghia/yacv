package com.uet.nvmnghia.yacv.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ComicPageViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    val parser = ComicParser(getApplication(),
        savedStateHandle.get<String>(ComicPageViewerFragment.COMIC_URI)!!)
    val pageNum = savedStateHandle.get<Int>(ComicPageViewerFragment.PAGE_NUM)!!

}