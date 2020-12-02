package com.uet.nvmnghia.yacv.model.folder

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface FolderDao {
    /**
     * Save without checking duplicate.
     * Only suitable for internal use.
     */
    @Insert
    fun saveUnsafe(folder: Folder): Long

    /**
     * The same as the overloaded method.
     */
    @Insert
    fun saveUnsafe(folders: List<Folder>): List<Long>

    /**
     * Save with checking duplicate.
     */
    @Transaction
    fun saveIfNotExisting(folderPath: String): Long {
        val id = getExistingId(folderPath)
        return if (id.isNotEmpty()) {
            id[0]
        } else {
            return saveUnsafe(Folder(folderPath))
        }
    }

    /**
     * Same as the overloaded method.
     */
    @Transaction
    fun saveIfNotExisting(folderPaths: Iterable<String>): List<Long> {
        return folderPaths.map { folderPath -> saveIfNotExisting(folderPath) }
    }

    /**
     * Deduplicate, then save.
     * Returns a [HashMap] that maps a folder path to its ID.
     */
    fun dedupThenSaveIfNotExist(folderPaths: Iterable<String>): HashMap<String, Long> {
        val folderPathSet = folderPaths.toSet()
        val folderIds = saveIfNotExisting(folderPathSet)

        var counter = 0
        val mapFolderPathToId = HashMap<String, Long>()
        folderPathSet.forEach { folderPath -> mapFolderPathToId[folderPath] = folderIds[counter++] }

        return mapFolderPathToId
    }

    @Query("SELECT * FROM Folder WHERE FolderID = :folderId")
    fun get(folderId: Int): LiveData<Folder>

    @Query("SELECT * FROM Folder")
    fun getAll(): LiveData<List<Folder>>

    /**
     * SELECT in this case does not seem to take advantage of UNIQUE constraint
     * and return a normal list of results.
     */
    @Query("SELECT FolderID FROM Folder WHERE FolderPath = :folderPath LIMIT 1")
    fun getExistingId(folderPath: String): List<Long>
}