package com.uet.nvmnghia.yacv.model.author

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.search.MetadataDao


@Dao
abstract class AuthorDao : MetadataDao<Author> {
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

    @Query("SELECT Author.* FROM Author INNER JOIN AuthorFts ON Author.AuthorID = AuthorFts.docid WHERE AuthorFts.Name MATCH :name LIMIT :limit")
    abstract fun searchByName(name: String, limit: Int = Int.MAX_VALUE): List<Author>

    override fun search(name: String, limit: Int): List<Author> {
        return searchByName(name, limit)
    }

    // Note that one author could have multiple roles even in a single comic
    @Query("SELECT DISTINCT Comic.ComicID, Comic.Title, Comic.FileUri FROM Comic INNER JOIN ComicAuthorJoin ON Comic.ComicID = ComicAuthorJoin.ComicID WHERE ComicAuthorJoin.AuthorID = :id")
    abstract override fun searchComic(id: Long): List<ComicMini>

    @Query("DELETE FROM Author")
    abstract fun truncate()
}