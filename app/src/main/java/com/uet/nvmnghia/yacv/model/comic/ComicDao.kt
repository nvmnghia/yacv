package com.uet.nvmnghia.yacv.model.comic

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.uet.nvmnghia.yacv.model.AppDatabase
import java.io.File
import java.io.IOException


/**
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */

@Dao
abstract class ComicDao(private val appDatabase: AppDatabase) {
    /**
     * Save without checking for foreign keys.
     * Only suitable for internal use.
     */
    @Insert
    protected abstract fun saveUnsafe(comic: Comic): Long

    /**
     * The same as the overloaded method.
     */
    @Insert
    protected abstract fun saveUnsafe(comics: List<Comic>): List<Long>

    @Transaction
    open    // By default, class methods are final (not overridable)
    fun save(comic: Comic): Long {
        comic.folderId = appDatabase.folderDao()
            .saveIfNotExisting(comic.parentFolderPath)
        return saveUnsafe(comic)
    }

    @Transaction
    open fun save(comics: List<Comic>): List<Long> {
        // Normal implementation: use existing save()
        // Yes I'm sadistic
//        return comics.map { comic -> save(comic) }

        // Map parent folder to ID
        val parentFolders = comics.map { comic -> comic.parentFolderPath }.toSet().toList()
        val parentFolderIds = appDatabase.folderDao().saveIfNotExisting(parentFolders)
        val mapParentFolderId = HashMap<String, Long>()
        for (i in parentFolders.indices) {
            mapParentFolderId[parentFolders[i]] = parentFolderIds[i]
        }

        // Update comic instances
        comics.map { comic -> comic.folderId = mapParentFolderId[comic.parentFolderPath]!! }

        return saveUnsafe(comics)
    }

    // Room is smart: only queries if there's observer
    @Query("SELECT * FROM comic WHERE id = :comicId")
    abstract fun get(comicId: String): LiveData<Comic>

    @Query("SELECT * FROM comic")
    abstract fun getAll(): LiveData<List<Comic>>

    @Query("SELECT * FROM Comic WHERE folder_id = :folderId")
    abstract fun getComicsInFolder(folderId: Int): LiveData<List<Comic>>

    @Query("SELECT * FROM Comic WHERE folder_id = :folderId LIMIT 1")
    abstract fun getFirstComicInFolder(folderId: Int): LiveData<Comic>

    @Query("SELECT COUNT(*) FROM comic WHERE path = :filePath")
    abstract fun getNumberOfMatch(filePath: String): Int
}