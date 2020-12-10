package com.uet.nvmnghia.yacv.model.folder

import androidx.lifecycle.LiveData
import com.uet.nvmnghia.yacv.model.comic.ComicRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FolderRepository @Inject constructor(
    private val folderDao: FolderDao,
    val comicRepo: ComicRepository,
) {
    /**
     * Get comics from DB.
     * This method DOES NOT rescan.
     */
    fun getFolders(): LiveData<List<Folder>> {
        return folderDao.getAll()
    }
}