package com.uet.nvmnghia.yacv.model.author

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4


@Entity
@Fts4(contentEntity = Author::class)    // Again: FTS doesn't have constraint, so no JOIN,
data class AuthorFts(                   // so an external content FTS is needed
    @ColumnInfo(name = Author.COLUMN_AUTHOR_NAME)
    val name: String,
)