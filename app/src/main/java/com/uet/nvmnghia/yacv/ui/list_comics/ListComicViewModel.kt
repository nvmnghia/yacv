package com.uet.nvmnghia.yacv.ui.list_comics

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.uet.nvmnghia.yacv.model.comic.ComicRepository


class ListComicViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,    // Access Fragment/Activity args
    application: Application,
    comicRepo: ComicRepository,
): AndroidViewModel(application) {

    private val folderUri: String = savedStateHandle.get<String>("folderUri")
        ?: throw IllegalArgumentException("Missing folder URI when browsing comics in folder")

    val comics = comicRepo.getComicsInFolder(folderUri)

}