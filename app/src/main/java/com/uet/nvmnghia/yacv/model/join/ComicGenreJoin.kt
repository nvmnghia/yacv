package com.uet.nvmnghia.yacv.model.join

import androidx.room.*
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.model.genre.Genre


@Entity(
    primaryKeys = ["ComicID", "GenreID"],
    indices = [Index(value = ["GenreID"])],
    foreignKeys = [
        ForeignKey(entity = Comic::class,
            parentColumns = ["ComicID"],
            childColumns = ["ComicID"]),
        ForeignKey(entity = Genre::class,
            parentColumns = ["GenreID"],
            childColumns = ["GenreID"])
    ]
)
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