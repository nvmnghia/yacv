package com.uet.nvmnghia.yacv.model.genre

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Genre(
    @ColumnInfo(name = "Name")
    val name: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_GENRE_ID)
    var id: Long = 0

    companion object {
        const val COLUMN_GENRE_ID = "GenreID"

        internal const val COLUMN_GENRE_NAME = "Name"
    }
}