package com.uet.nvmnghia.yacv.model.author

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction


@Dao
abstract class AuthorDao {
    /**
     * Save without checking duplicate.
     * Only suitable for internal use.
     */
    @Insert
    protected abstract fun saveUnsafe(author: Author): Long

    /**
     * Same as the overloaded method.
     */
    @Insert
    protected abstract fun saveUnsafe(authors: List<Author>): List<Long>

    /**
     * Save with checking duplicate.
     */
    @Transaction
    open fun saveIfAbsent(name: String): Long {
        val trimmedName = name.trim()
        val id = searchIdByName(trimmedName)
        return if (id.isNotEmpty()) {
            id[0]
        } else {
            saveUnsafe(Author(trimmedName))
        }
    }

    /**
     * Same as the overloaded method.
     */
    @Transaction
    open fun saveIfAbsent(names: Iterable<String>): List<Long> {
        return names.map { name -> saveIfAbsent(name) }
    }

    @Query("SELECT * FROM Author WHERE AuthorID = :authorId")
    abstract fun get(authorId: Long): LiveData<Author>

    @Query("SELECT * FROM Author")
    abstract fun getAll(): LiveData<List<Author>>

    @Query("SELECT docid FROM AuthorFts WHERE Name MATCH :name")
    abstract fun searchIdByName(name: String): List<Long>
}