package com.uet.nvmnghia.yacv.ui.library

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.folder.FolderRepository
import com.uet.nvmnghia.yacv.utils.Constants


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
    private val folderRepository: FolderRepository, application: Application
) : AndroidViewModel(application) {

    private val sharedPref = application.getSharedPreferences(
        application.resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    private var rootFolderUri = sharedPref
        .getString(Constants.SHPREF_KEY_ROOT_FOLDER, Constants.DEFAULT_ROOT_FOLDER)
        ?.let { Uri.parse(it) }

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

    /**
     * Set [rootFolderUri] and persist it in SharedPreference.
     */
    private fun setDirectory(uri: Uri) {
        rootFolderUri = uri

        with (sharedPref.edit()) {
            putString(Constants.SHPREF_KEY_ROOT_FOLDER, rootFolderUri.toString())
            apply()
        }
    }

    /**
     * Given a non-null uri, make it the [rootFolderUri] and scan its files.
     * If the uri is null, quickly rescan the current one.
     */
    fun rescanComics(uri: Uri? = null) {
        uri?.let { setDirectory(it) }
        rootFolderUri?.let { folderRepository.rescanComics(it, uri == null) }
    }
}
