package com.uet.nvmnghia.yacv.ui.reader

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ComicPageViewerViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    val parser = ComicParser(getApplication(),
        savedStateHandle.get<String>(ComicPageViewerFragment.COMIC_URI)!!)
    val pageNum = savedStateHandle.get<Int>(ComicPageViewerFragment.PAGE_NUM)!!

}