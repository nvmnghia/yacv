package com.uet.nvmnghia.yacv.ui.metadata

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import javax.inject.Inject


class MetadataViewModel @Inject constructor(
    private val comicDao: ComicDao
) : ViewModel() {

    lateinit var comic: LiveData<Comic>

    fun setComicID(comicID: Long) {
        comic = comicDao.get(comicID)
    }

}