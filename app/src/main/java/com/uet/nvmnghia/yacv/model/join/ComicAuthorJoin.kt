package com.uet.nvmnghia.yacv.model.join

import androidx.room.*
import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.author.RoleTable
import com.uet.nvmnghia.yacv.model.comic.Comic


@Entity(
    primaryKeys = [
        Comic.COLUMN_COMIC_ID,
        Author.COLUMN_AUTHOR_ID,
        RoleTable.COLUMN_ROLE_ID,
    ],
    indices = [
        Index(value = [Author.COLUMN_AUTHOR_ID]),
        Index(value = [RoleTable.COLUMN_ROLE_ID])    // TODO: Is this index necessary?
    ],
    foreignKeys = [
        ForeignKey(entity = Comic::class,
            parentColumns = [Comic.COLUMN_COMIC_ID],
            childColumns  = [Comic.COLUMN_COMIC_ID]),
        ForeignKey(entity = Author::class,
            parentColumns = [Author.COLUMN_AUTHOR_ID],
            childColumns  = [Author.COLUMN_AUTHOR_ID]),
        ForeignKey(entity = RoleTable::class,
            parentColumns = [RoleTable.COLUMN_ROLE_ID],
            childColumns  = [RoleTable.COLUMN_ROLE_ID])
    ]
)
data class ComicAuthorJoin(
    @ColumnInfo(name = Comic.COLUMN_COMIC_ID)
    val comicId: Long,
    @ColumnInfo(name = Author.COLUMN_AUTHOR_ID)
    val authorId: Long,
    @ColumnInfo(name = RoleTable.COLUMN_ROLE_ID)
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