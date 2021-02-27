package com.uet.nvmnghia.yacv.model.genre

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.search.MetadataDao


@Dao
abstract class GenreDao : MetadataDao<Genre> {
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

    @Query("INSERT INTO Genre(Name) SELECT :name WHERE NOT EXISTS(SELECT 1 FROM GenreFts WHERE Name MATCH :name)")
    abstract fun saveIfAbsentNative(name: String): Long

    @Query("SELECT docid FROM GenreFts WHERE Name MATCH :name")
    abstract fun searchIdByName(name: String): List<Long>

    @Query("SELECT Genre.* FROM Genre INNER JOIN GenreFts ON Genre.GenreID = GenreFts.docid WHERE GenreFts.Name MATCH :name LIMIT :limit")
    abstract fun searchByName(name: String, limit: Int = Int.MAX_VALUE): List<Genre>

    override fun search(name: String, limit: Int): List<Genre> {
        return searchByName(name, limit)
    }

    @Query("SELECT Comic.ComicID, Comic.Title, Comic.FileUri FROM Comic JOIN ComicGenreJoin ON Comic.ComicID = ComicGenreJoin.ComicID WHERE ComicGenreJoin.GenreID = :id")
    abstract override fun searchComic(id: Long): List<ComicMini>

    @Query("DELETE FROM Genre")
    abstract fun truncate()
}