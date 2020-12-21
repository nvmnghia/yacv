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
import com.uet.nvmnghia.yacv.utils.FileUtils


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

    // ViewModel communicates with View by LiveData, instead of direct control
    val folders: LiveData<List<Folder>> = folderRepo.getFolders()


    //================================================================================
    // Text state
    //================================================================================

    enum class TextState {
        // List displayed normally
        NO_TEXT,

        // List is not displayed, and text is
        NO_ROOT_FOLDER, CANNOT_READ_ROOT_FOLDER, NO_READ_PERMISSION,
        NO_READ_PERMISSION_FOREVER,    // No more asking
        NO_COMIC,
    }

    /**
     * State for the text if list comic folders is not displayed.
     */
    var textState =                      // https://developer.android.com/topic/libraries/architecture/viewmodel#implement
        MediatorLiveData<TextState>()    // ViewModel shouldn't observe LiveData, instead use MediatorLiveData or transformation.

    init {
        textState.addSource(folders, this::folderListChanged)
    }

    private fun folderListChanged(folderList: List<Folder>) {
        if (folderList.isEmpty()) {
            if (textState.value == TextState.NO_TEXT) {
                textState.value = TextState.NO_COMIC
            }
        } else {
            if (textState.value == TextState.NO_COMIC) {
                textState.value = TextState.NO_TEXT
            }
        }
    }

    private fun rootFolderChanged(newRootFolderUri: Uri?) {
        textState.value = newRootFolderUri.let {
            when {
                it == null -> TextState.NO_ROOT_FOLDER
                ! FileUtils.canReadTree(getApplication(), it) -> TextState.CANNOT_READ_ROOT_FOLDER
                else -> {
                    scanComics(deep = true, truncateOld = true)
                    TextState.NO_TEXT
                }
            }
        }
    }

    /**
     * TODO: Somehow merge these readPermission methods together.
     */
    fun readPermissionNotGranted() {
        if (textState.value != TextState.NO_ROOT_FOLDER) {
            textState.value = TextState.NO_READ_PERMISSION
        }
    }

    fun readPermissionGranted() {
        if (rootFolderUri == null) {
            textState.value = TextState.NO_ROOT_FOLDER
        } else if (folders.value == null || folders.value!!.isEmpty()) {
            textState.value = TextState.NO_COMIC
        } else {
            textState.value = TextState.NO_TEXT
        }
    }

    fun readPermissionNotGrantedForever() {
        textState.value = TextState.NO_READ_PERMISSION_FOREVER
    }


    //================================================================================
    // Root folder state
    //================================================================================

    // Initialization DOES NOT call custom setter.
    private var rootFolderUri: Uri? = null
        set(newRootFolderUri) {
            field = newRootFolderUri

            rootFolderChanged(newRootFolderUri)

            // Upon initialization, the SharedPreference is written back unnecessarily.
            with(sharedPref.edit()) {
                putString(Constants.SHPREF_ROOT_FOLDER, field.toString())
                apply()
            }
        }

    init {
        // https://medium.com/keepsafe-engineering/an-in-depth-look-at-kotlins-initializers-a0420fcbf546
        // rootFolderUri must be set after textState initialization, as inside its setter, textState is used.
        rootFolderUri = sharedPref.getString(Constants.SHPREF_ROOT_FOLDER, null)?.let { Uri.parse(it) }
    }

    /**
     * Given an URI, convert it to a native path and assign the result to [rootFolderUri].
     * If the converted path is the same as [rootFolderUri], only do a quick rescan.
     */
    fun changeRootFolder(newFolderUri: Uri) {
        if (newFolderUri == rootFolderUri) {
            // rootFolder doesn't change, do a deep rescan then
            rescanComics(true)
        } else {
            // Rescan & shit is done inside setter
            rootFolderUri = newFolderUri
        }
    }


    //================================================================================
    // Other functions
    //================================================================================

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
