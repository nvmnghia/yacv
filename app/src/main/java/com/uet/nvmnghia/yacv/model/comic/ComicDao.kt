package com.uet.nvmnghia.yacv.model.comic

import androidx.lifecycle.LiveData
import androidx.room.*


/**
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */

@Dao
interface ComicDao {
    /**
     * Save a [Comic] instance without checking duplicate path.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUnsafe(comic: Comic)

    /**
     * Save a [Comic] instance only if path is not duplicate.
     */
    @Transaction
    open suspend fun save(comic: Comic) {
        if (getNumberOfMatch(comic.path) == 0) {
            saveUnsafe(comic)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUnsafe(comics: List<Comic>)

    // Room is smart: only queries if there's observer
    @Query("SELECT rowid, * FROM comic WHERE rowid = :comicId")
    fun load(comicId: String) : LiveData<Comic>

    @Query("SELECT rowid, * FROM comic")
    fun load() : LiveData<List<Comic>>

    @Query("SELECT COUNT(*) FROM comic WHERE path = :filePath")
    fun getNumberOfMatch(filePath: String) : Int
}