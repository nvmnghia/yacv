package com.uet.nvmnghia.yacv.ui.reader

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.uet.nvmnghia.yacv.model.comic.ComicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    comicRepo: ComicRepository
) : ViewModel() {

    val comicID = savedStateHandle.get<Long>("comicID")
        ?: throw IllegalArgumentException("Missing ComicID when reading comic.")

    val comic = comicRepo.getComic(comicID)

    val fileName = Transformations.map(comic) { comic ->
        Uri.parse(comic.fileUri).path?.substringAfterLast('/')
    }

}