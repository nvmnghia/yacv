package com.uet.nvmnghia.yacv.model.author

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4


@Entity
@Fts4(contentEntity = Author::class)
data class AuthorFts(
    @ColumnInfo(name = "Name")
    val name: String,
)