package com.uet.nvmnghia.yacv.model.genre

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey


@Entity
data class Genre(
    @ColumnInfo(name = "Name")
    val name: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "GenreID")
    var id: Long = 0
}