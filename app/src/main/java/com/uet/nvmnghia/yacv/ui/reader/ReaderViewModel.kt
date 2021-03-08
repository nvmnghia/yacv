package com.uet.nvmnghia.yacv.ui.reader

import android.net.Uri
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.uet.nvmnghia.yacv.model.comic.ComicRepository

class ReaderViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,
    comicRepo: ComicRepository
) : ViewModel() {

    val comicID = savedStateHandle.get<Long>("comicID")
        ?: throw IllegalArgumentException("Missing ComicID when reading comic.")

    val comic = comicRepo.getComic(comicID)

    val fileName = Transformations.map(comic) { comic ->
        Uri.parse(comic.fileUri).path?.substringAfterLast('/')
    }

}