package com.uet.nvmnghia.yacv.model.series

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction


@Dao
abstract class SeriesDao {
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
}