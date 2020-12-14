package com.uet.nvmnghia.yacv.ui.library

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.model.comic.ComicRepository
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.folder.FolderRepository


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
    private val folderRepository: FolderRepository
) : ViewModel() {

    private lateinit var rootScanDirectory: String
    val folders: LiveData<List<Folder>> = folderRepository.getFolders()

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
        rootScanDirectory = dir
    }

    fun rescanComics() {
        folderRepository.rescanComics()
    }
}