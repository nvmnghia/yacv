package com.uet.nvmnghia.yacv.model.join

import androidx.room.*
import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.author.PositionTable
import com.uet.nvmnghia.yacv.model.comic.Comic


@Entity(
    primaryKeys = ["ComicID", "AuthorID", "PositionID"],
    indices = [
        Index(value = ["AuthorID"]),
        Index(value = ["PositionID"])    // TODO: Is this index necessary?
    ],
    foreignKeys = [
        ForeignKey(entity = Comic::class,
            parentColumns = ["ComicID"],
            childColumns = ["ComicID"]),
        ForeignKey(entity = Author::class,
            parentColumns = ["AuthorID"],
            childColumns = ["AuthorID"]),
        ForeignKey(entity = PositionTable::class,
            parentColumns = ["PositionID"],
            childColumns = ["PositionID"])
    ]
)
data class ComicAuthorJoin(
    @ColumnInfo(name = "ComicID")
    val comicId: Long,
    @ColumnInfo(name = "AuthorID")
    val authorId: Long,
    @ColumnInfo(name = "PositionID")
    val positionId: Long,
)


@Dao
interface ComicAuthorJoinDao {
    @Insert
    fun save(join: ComicAuthorJoin)

    fun save(comicId: Long, authorId: Long, positionId: Long) {
        save(ComicAuthorJoin(comicId, authorId, positionId))
    }
}