package com.uet.nvmnghia.yacv.model.comic

import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.parser.ComicScanner
import com.uet.nvmnghia.yacv.parser.file.ComicParserFactory
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
                val comics = documents
                    .filterNotNull()
                    .mapNotNull { document ->
                        ComicParserFactory.create(comicScanner.context, document)?.info }
                comicDao.save(comics)
            }
        }
    }
}