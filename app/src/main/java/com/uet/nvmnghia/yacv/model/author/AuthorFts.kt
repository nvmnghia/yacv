package com.uet.nvmnghia.yacv.model.author

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4


@Entity
@Fts4(contentEntity = Author::class)
data class AuthorFts(
    @ColumnInfo(name = Author.COLUMN_AUTHOR_NAME)
    val name: String,
) {

    companion object {
        const val COLUMN_AUTHOR_ID = "AuthorID"

        internal const val COLUMN_AUTHOR_NAME = "Name"
    }

}