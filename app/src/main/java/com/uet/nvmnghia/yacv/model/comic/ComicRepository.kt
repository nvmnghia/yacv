package com.uet.nvmnghia.yacv.model.comic

import androidx.lifecycle.LiveData
import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.parser.ComicScanner
import com.uet.nvmnghia.yacv.parser.file.ComicParserFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
    private val appDb: AppDatabase
) {
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
    fun scanComics(rootFolder: String, deep: Boolean, truncateOld: Boolean) {
        val _deep = if (truncateOld) {
            true
        } else {
            deep
        }

        // Run in background
        CoroutineScope(Dispatchers.IO).launch {
            if (truncateOld) {
                appDb.resetDb()
            }

            ComicScanner.scan(rootFolder, _deep).collect { files ->
                comicDao.save(
                    files
                        .filterNotNull()
                        .map { file -> ComicParserFactory.create(file).info }
                )
            }
        }
    }
}