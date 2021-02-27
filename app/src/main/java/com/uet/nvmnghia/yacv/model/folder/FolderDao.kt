package com.uet.nvmnghia.yacv.model.folder

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.search.MetadataDao


@Dao
interface FolderDao : MetadataDao<Folder> {
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
    fun saveIfAbsent(folderUri: String, folderName: String): Long {
        val id = getExistingId(folderUri)
        return if (id.isNotEmpty()) {
            id[0]
        } else {
            return saveUnsafe(Folder(folderUri, folderName))
        }
    }

    /**
     * Same as the overloaded method.
     */
    @Transaction
    fun saveIfAbsent(folderUris: Iterable<String>, folderNames: Iterable<String>): List<Long> {
        val folderUrisItr = folderUris.iterator()
        val folderNamesItr = folderNames.iterator()
        val folderIds = ArrayList<Long>()

        while (folderUrisItr.hasNext()) {
            folderIds.add(saveIfAbsent(folderUrisItr.next(), folderNamesItr.next()))
        }

        return folderIds
    }

    /**
     * Deduplicate, then save.
     * Returns a [HashMap] that maps a folder path to its ID.
     */
    fun dedupThenSaveIfAbsent(folderUris: Iterable<String>, folderNames: Iterable<String>): Map<String, Long> {
        val folderUrisItr = folderUris.iterator()
        val folderNamesItr = folderNames.iterator()
        val mapUri2Name = mutableMapOf<String, String>()

        while (folderUrisItr.hasNext()) {
            val folderUri = folderNamesItr.next()
            val folderName = folderNamesItr.next()

            if (mapUri2Name[folderUri] == null) {
                mapUri2Name[folderUri] = folderName
            }
        }

        val folderIds = mapUri2Name.map { entry -> saveIfAbsent(entry.key, entry.value) }
        var idx = 0
        val mapUri2Id = mutableMapOf<String, Long>()
        mapUri2Name.forEach { entry -> mapUri2Id[entry.key] = folderIds[idx++] }

        return mapUri2Id
    }

    @Query("SELECT * FROM Folder WHERE FolderID = :folderId")
    fun get(folderId: Int): LiveData<Folder>

    @Query("SELECT * FROM Folder")
    fun getAll(): LiveData<List<Folder>>

    // SELECT in this case does not seem to take advantage of UNIQUE constraint
    // and returns a normal list of results.
    @Query("SELECT FolderID FROM Folder WHERE FolderUri = :folderUri LIMIT 1")
    fun getExistingId(folderUri: String): List<Long>

    @Query("SELECT Folder.* FROM Folder INNER JOIN FolderFTS ON Folder.FolderID = FolderFTS.docid WHERE FolderFTS.Name MATCH :name LIMIT :limit")
    fun searchByName(name: String, limit: Int): List<Folder>

    override fun search(name: String, limit: Int): List<Folder> = searchByName(name, limit)

    @Query("SELECT Comic.ComicID, Comic.Title, Comic.FileUri FROM Comic WHERE Comic.FolderID = :id")
    override fun searchComic(id: Long): List<ComicMini>

    @Query("DELETE FROM Folder")
    fun truncate()
}