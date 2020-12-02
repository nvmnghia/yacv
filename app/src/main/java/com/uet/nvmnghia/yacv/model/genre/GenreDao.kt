package com.uet.nvmnghia.yacv.model.genre

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
abstract class GenreDao {
    /**
     * Save without checking duplicate.
     * Only suitable for internal use.
     */
    @Insert
    protected abstract fun saveUnsafe(genre: Genre): Long

    /**
     * Same as the overloaded method.
     */
    @Insert
    protected abstract fun saveUnsafe(genres: List<Genre>): List<Long>

    /**
     * Save with checking duplicate.
     */
    open fun saveIfAbsent(name: String): Long {
        val trimmedName = name.trim()
        val id = searchIdByName(trimmedName)
        return if (id.isNotEmpty()) {
            id[0]
        } else {
            saveUnsafe(Genre(name))
        }
    }

    /**
     * Same as the overloaded method.
     */
    open fun saveIfAbsent(names: Iterable<String>): List<Long> {
        return names.map { name -> saveIfAbsent(name) }
    }

    @Query("INSERT INTO Genre(Name) SELECT :name WHERE NOT EXISTS(SELECT 1 FROM Genre WHERE Name MATCH :name)")
    abstract fun saveIfAbsentNative(name: String): Long

    @Query("SELECT rowid FROM Genre WHERE Name MATCH :name")
    abstract fun searchIdByName(name: String): List<Long>
}