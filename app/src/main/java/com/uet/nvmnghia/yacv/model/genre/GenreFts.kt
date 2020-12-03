package com.uet.nvmnghia.yacv.model.genre

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4


@Entity
@Fts4(contentEntity = Genre::class)
data class GenreFts(
    @ColumnInfo(name = "Name")
    val name: String
)