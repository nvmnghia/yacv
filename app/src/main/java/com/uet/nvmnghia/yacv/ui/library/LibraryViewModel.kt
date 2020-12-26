package com.uet.nvmnghia.yacv.ui.library

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.cancelAndJoin


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
     * Whether the read permission is granted.
     */
    var readPermissionGranted: Boolean = false    // Meaningless initial value to use var with custom setter
        // A custom getter can guarantee to be always correct by checking directly
        // but it seems to be expensive
        // Manually check & set seems to be a reasonable choice
        // TODO: compare with custom getter
        set(granted) {
            if (granted) {
                readPermissionDeniedForever = false

                when {
                    !rootFolderSelected -> textState.value = TextState.NO_ROOT
                    !rootFolderExists -> textState.value = TextState.HAVE_ROOT_NOT_EXIST
//                    folders.value?.isEmpty() == true -> TextState.NO_COMIC
//                    else -> TextState.NO_TEXT
                    else -> {
                        if (field != granted) {
                            rescanComics(false)
                            Log.w("yacv", "Rescan triggered by flip readPermissionGranted to true")
                        }
                    }
                }
            } else {
                if (rootFolderExists) {
                    textState.value = TextState.HAVE_ROOT_NO_PERMISSION
                }
            }

            field = granted
        }

    /**
     * Whether the Never ask again box is checked.
     * As there's no reliable way to check this, it is persisted.
     */
    var readPermissionDeniedForever: Boolean = sharedPref.getBoolean(Constants.SHPREF_READ_PERMISSION_DENIED_FOREVER, false)
        set(deniedForever) {
            field = deniedForever

            with(sharedPref.edit()) {
                putBoolean(Constants.SHPREF_READ_PERMISSION_DENIED_FOREVER, deniedForever)
                apply()
            }

            if (deniedForever) {
                textState.value = TextState.NO_READ_PERMISSION_FOREVER
            }
        }

    /**
     * Whether a folder is selected as root.
     */
    private var rootFolderSelected: Boolean = false    // Meaningless initial value to use var with custom setter
        set(selected) {
            field = selected

            if (!selected && !readPermissionDeniedForever) {
                textState.value = TextState.NO_ROOT
            }

            rootFolderExists = true
        }

    /**
     * Whether the selected root folder can be read.
     */
    private var rootFolderExists = false
        set(exist) {
            field = exist

            if (!exist && rootFolderSelected && readPermissionGranted) {
                textState.value = TextState.HAVE_ROOT_NOT_EXIST
            }
        }

    /**
     * Enum for all possible states of the text if list folders is not displayed.
     */
    enum class TextState {
        // List displayed normally
        NO_TEXT,

        // List is not displayed, and text is
        NO_ROOT,
        HAVE_ROOT_NO_PERMISSION,       // Had root folder, but cannot read due to revoked permission
        HAVE_ROOT_NOT_EXIST,           // Had root folder, but cannot find it anymore
        NO_READ_PERMISSION_FOREVER,    // No more asking
        NO_COMIC
    }

    var textState =                      // https://developer.android.com/topic/libraries/architecture/viewmodel#implement
        MediatorLiveData<TextState>()    // ViewModel shouldn't observe LiveData, instead use MediatorLiveData or transformation.

    init {
        // Initialization order matters
        rootFolderSelected = rootFolderUri != null

        val documentFile = rootFolderUri?.let { DocumentFile.fromTreeUri(getApplication(), it) }
        rootFolderExists = documentFile?.exists() ?: false

        readPermissionGranted = ContextCompat.checkSelfPermission(getApplication(),
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        textState.addSource(folders) { newListFolders ->
            if (readPermissionGranted && rootFolderExists) {
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
