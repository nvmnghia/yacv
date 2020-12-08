package com.uet.nvmnghia.yacv.model.folder

import android.net.Uri
import androidx.lifecycle.LiveData
import com.uet.nvmnghia.yacv.model.comic.ComicRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FolderRepository @Inject constructor(
    private val folderDao: FolderDao,
    private val comicRepository: ComicRepository,
) {
    fun getFolders(rescan: Boolean = false): LiveData<List<Folder>> {
        if (rescan) comicRepository.rescanComics()
        return folderDao.getAll()
    }

    /**
     * Given a non-null folder uri, scan its files.
     * If [newRoot] is true, delete data from all tables.
     */
    fun rescanComics(rootUri: Uri, newRoot: Boolean? = false) {
        comicRepository.rescanComics(rootUri, newRoot)
    }
}