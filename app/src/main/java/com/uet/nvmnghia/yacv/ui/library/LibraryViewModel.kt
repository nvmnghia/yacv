package com.uet.nvmnghia.yacv.ui.library

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.model.comic.ComicRepository


/**
 * ViewModel persists data between Activity/Fragment recreation
 *
 * To achieve this design decision:
 * - A ViewModel MUST be created by ViewModelProvider
 *   https://developer.android.com/codelabs/kotlin-android-training-view-model#4
 * - Move all data & data processing to ViewModel
 *   BUT NOT anything referencing views
 */

/**
 * ViewModel for LibraryFragment
 * Currently used to test compression reading code
 */
class LibraryViewModel @ViewModelInject constructor(
    private val comicRepository: ComicRepository
) : ViewModel() {

    private lateinit var directory: String
    val comics: LiveData<List<Comic>> = comicRepository.getComics()

    /**
     * Primary constructor
     * Several init block will be merged as one
     */
    init {

    }

    override fun onCleared() {
        super.onCleared()
        // Destroy shit here
    }

    fun setDirectory(dir: String) {
        directory = dir
    }

    fun rescanComics() {
        comicRepository.rescanComics()
    }
}