package com.uet.nvmnghia.yacv.model.comic

import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.parser.ComicScanner
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Singleton


// An object instance managed by Hilt needs to be stored in a container
// like bean & IoC container in Spring
// This annotation scopes an instance of DBProvider to application container
// therefore that exact & only instance is provided anywhere needed.
// Of course there're more scoping annotations
@Singleton
class ComicRepository
// @Inject in field declaration:       inject something into this field
// @Inject in constructor declaration: inject this class somewhere, init by this constructor
@Inject constructor(
    private val comicDao: ComicDao,
    private val appDb: AppDatabase,
    private val comicScanner: ComicScanner,
) {
    private var currentScanningJob: Job? = null

    /**
     * Get comics from DB.
     * This method DOES NOT rescan.
     */
    fun getComics(): LiveData<List<Comic>> {
        return comicDao.getAll()
    }

    /**
     * Given a [comicID], get a single [Comic] of the given ID.
     */
    fun getComic(comicID: Long): LiveData<Comic> {
        return comicDao.get(comicID)
    }

    /**
     * Given the folder URI, returns the comics inside that folders.
     */
    fun getComicsInFolder(folderUri: String): LiveData<List<Comic>> {
        return comicDao.getComicsInFolder(folderUri)
    }

    /**
     * Scan the given folder for comics.
     * When [truncateOld] is set, [deep] is also set.
     *
     * @param rootFolder Folder to scan for comics
     * @param deep Scan deeply, slower but guarantee to scan all files
     * @param truncateOld Truncate old data, used when a new root folder is selected
     */
    fun scanComics(rootFolder: DocumentFile, deep: Boolean, truncateOld: Boolean) {
        val _deep = if (truncateOld) {
            true
        } else {
            deep
        }

        runBlocking {
            currentScanningJob?.apply { cancelAndJoin() }
        }

        // Run in background
        currentScanningJob = CoroutineScope(Dispatchers.IO).launch {
            if (truncateOld) {
                appDb.resetDb()
            }

            if (!_deep) {
                return@launch
            }

            comicScanner.scan(rootFolder, this).collect { documents ->
                // TODO: this one always parse metadata, maybe unnecessarily
                val comics = documents
                    .filterNotNull()
                    .mapNotNull { document ->
                        ComicParser(comicScanner.context, document.uri).metadata }
                comicDao.saveIfAbsent(comics)
            }
        }
    }
}