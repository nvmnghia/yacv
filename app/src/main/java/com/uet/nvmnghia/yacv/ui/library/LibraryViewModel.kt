package com.uet.nvmnghia.yacv.ui.library

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.folder.FolderRepository
import com.uet.nvmnghia.yacv.utils.Constants
import kotlinx.coroutines.Job


/**
 * ViewModel persists data between Activity/Fragment recreation
 *
 * To achieve this design decision:
 * - A ViewModel MUST be created by ViewModelProvider
 *   https://developer.android.com/codelabs/kotlin-android-training-view-model#4
 * - Move all data & data processing to ViewModel
 *   BUT NOT anything referencing views
 */


class LibraryViewModel @ViewModelInject constructor(
    private val folderRepo: FolderRepository, application: Application,
) : AndroidViewModel(application) {

    private val sharedPref = application.getSharedPreferences(
        application.resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

    var rootFolderUri: Uri? = sharedPref.getString(Constants.SHPREF_ROOT_FOLDER, null)?.let { Uri.parse(it) }
        set(uri) {
            if (field == uri) {
                rescanComics(true)
            } else {
                field = uri
                scanComics(deep = true, truncateOld = true)
                with(sharedPref.edit()) {
                    putString(Constants.SHPREF_ROOT_FOLDER, uri.toString())
                    apply()
                }
            }

            rootFolderSelected = uri != null
        }

    // ViewModel communicates with View by LiveData, instead of direct control
    val folders: LiveData<List<Folder>> = folderRepo.getFolders()


    //================================================================================
    // Text state
    //================================================================================

    /**
     * Whether a folder is selected as root.
     */
    private var rootFolderSelected: Boolean = false    // Meaningless initial value to use var with custom setter
        set(selected) {
            field = selected

            if (!selected) {
                textState.value = TextState.NO_ROOT
            } else {
                rootFolderExists = true
            }
        }

    /**
     * Whether the selected root folder can be read.
     */
    private var rootFolderExists = false
        set(exist) {
            field = exist

            if (!exist && rootFolderSelected) {
                textState.value = TextState.ROOT_NOT_FOUND
            }
        }

    /**
     * Enum for all possible states of the text if list folders is not displayed.
     */
    enum class TextState {
        // List displayed normally
        NO_TEXT,

        // List is not displayed, but text is
        NO_ROOT,
        ROOT_NOT_FOUND,    // Had root folder, but cannot find it anymore
        NO_COMIC
    }

    var textState =                      // https://developer.android.com/topic/libraries/architecture/viewmodel#implement
        MediatorLiveData<TextState>()    // ViewModel shouldn't observe LiveData, instead use MediatorLiveData or transformation.

    init {
        // Initialization order matters
        rootFolderSelected = rootFolderUri != null

        val documentFile = rootFolderUri?.let { DocumentFile.fromTreeUri(getApplication(), it) }
        rootFolderExists = documentFile?.exists() ?: false

        textState.addSource(folders) { newListFolders ->
            if (rootFolderExists) {
                textState.value = if (newListFolders.isEmpty()) TextState.NO_COMIC
                    else TextState.NO_TEXT
            }
        }
    }


    //================================================================================
    // Other functions
    //================================================================================

    private var currentScanningJob: Job? = null

    /**
     * A wrapper for ComicRepository's scanComics().
     */
    private fun scanComics(deep: Boolean, truncateOld: Boolean) {
        rootFolderUri?.let { folderRepo.comicRepo.scanComics(
            DocumentFile.fromTreeUri(getApplication(), it)!!, deep, truncateOld) }
    }

    /**
     * Rescan comics but not truncate database.
     * A wrapper for [scanComics].
     * This function is used when:
     * - Start the app:    without any argument
     * - Same root folder: without any argument
     * - Force rescan:     with [deep] is set
     */
    fun rescanComics(deep: Boolean = false) {
        scanComics(deep, truncateOld = false)
    }

    override fun onCleared() {
        super.onCleared()
        // Destroy shit here
    }
}
