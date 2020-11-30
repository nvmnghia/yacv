package com.uet.nvmnghia.yacv.model.folder

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface FolderDao {
    @Query("SELECT * FROM Folder WHERE id = :folderId")
    fun get(folderId: Int): LiveData<Folder>

    @Query("SELECT * FROM Folder")
    fun getAll(): LiveData<List<Folder>>

    /**
     * SELECT in this case does not seem to take advantage of UNIQUE constraint
     * and return a normal list of results.
     */
    @Query("SELECT id FROM Folder WHERE path = :folderPath")
    fun getExistingId(folderPath: String): List<Long>

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

    @Transaction
    fun saveIfNotExisting(folderPath: String): Long {
        return try {
            getExistingId(folderPath)[0]
        } catch (ie: IndexOutOfBoundsException) {
            saveUnsafe(Folder(folderPath))
        }
    }

    @Transaction
    fun saveIfNotExisting(folderPaths: List<String>): List<Long> {
        return folderPaths.map { folderPath -> saveIfNotExisting(folderPath) }
    }
}