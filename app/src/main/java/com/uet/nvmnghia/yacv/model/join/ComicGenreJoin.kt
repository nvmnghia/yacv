package com.uet.nvmnghia.yacv.model.join

import androidx.room.*


@Entity(primaryKeys = ["ComicID", "GenreID"])
data class ComicGenreJoin(
    @ColumnInfo(name = "ComicID")
    val comicId: Long,
    @ColumnInfo(name = "GenreID")
    val genreId: Long,
)


@Dao
interface ComicGenreJoinDao {
    @Insert
    fun save(join: ComicGenreJoin)

    fun save(comicId: Long, genreId: Long) {
        save(ComicGenreJoin(comicId, genreId))
    }

    @Transaction
    fun save(comicId: Long, genreIds: Iterable<Long>) {
        genreIds.toSet().forEach { genreId -> save(ComicGenreJoin(comicId, genreId)) }
    }
}