package com.uet.nvmnghia.yacv.model.series

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.search.MetadataDao


@Dao
abstract class SeriesDao : MetadataDao<Series> {
    /**
     * Save without checking duplicate.
     * Only suitable for internal use.
     */
    @Insert
    protected abstract fun saveUnsafe(series: Series): Long

    /**
     * Same as the overloaded method.
     */
    @Insert
    protected abstract fun saveUnsafe(series: List<Series>): List<Long>

    /**
     * Save with checking duplicate.
     */
    @Transaction
    open fun saveIfAbsent(name: String, volume: Int? = null, count: Int? = null, manga: Boolean? = null): Long {
        val trimmedName = name.trim()
        val id = searchIdByName(trimmedName)
        return if (id.isNotEmpty()) {
            id[0]
        } else {
            saveUnsafe(Series(trimmedName, volume, count, manga))
        }
    }

    @Query("SELECT * FROM Series WHERE SeriesID = :seriesId")
    abstract fun get(seriesId: Long): LiveData<Series>

    @Query("SELECT * FROM Series")
    abstract fun getAll(): LiveData<List<Series>>

    @Query("SELECT docid FROM SeriesFts WHERE Name MATCH :name")
    abstract fun searchIdByName(name: String): List<Long>

    @Query("SELECT Series.* FROM Series INNER JOIN SeriesFts ON Series.SeriesID = SeriesFts.docid WHERE SeriesFts.Name MATCH :name LIMIT :limit")
    abstract fun searchByName(name: String, limit: Int = Int.MAX_VALUE): List<Series>

    override fun search(name: String, limit: Int): List<Series> {
        return searchByName(name, limit)
    }

    @Query("SELECT ComicID, Title, FileUri FROM Comic WHERE SeriesID = :id")
    abstract override fun searchComic(id: Long): List<ComicMini>

    @Query("DELETE FROM Series")
    abstract fun truncate()
}