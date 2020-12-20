package com.uet.nvmnghia.yacv.model.join

import androidx.room.*
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.model.genre.Genre


@Entity(
    primaryKeys = [Comic.COLUMN_COMIC_ID, Genre.COLUMN_GENRE_ID],
    indices = [Index(value = [Genre.COLUMN_GENRE_ID])],
    foreignKeys = [
        ForeignKey(entity = Comic::class,
            parentColumns = [Comic.COLUMN_COMIC_ID],
            childColumns  = [Comic.COLUMN_COMIC_ID]),
        ForeignKey(entity = Genre::class,
            parentColumns = [Genre.COLUMN_GENRE_ID],
            childColumns  = [Genre.COLUMN_GENRE_ID])
    ]
)
data class ComicGenreJoin(
    @ColumnInfo(name = Comic.COLUMN_COMIC_ID)
    val comicId: Long,
    @ColumnInfo(name = Genre.COLUMN_GENRE_ID)
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

    @Query("DELETE FROM ComicGenreJoin")
    fun truncate()
}