package com.uet.nvmnghia.yacv.model.genre

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4


@Entity
@Fts4(contentEntity = Genre::class)
data class GenreFts(
    @ColumnInfo(name = Genre.COLUMN_GENRE_NAME)
    val name: String
)