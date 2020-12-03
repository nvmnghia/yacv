package com.uet.nvmnghia.yacv.model.series

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4


@Entity
@Fts4(contentEntity = Series::class)
data class SeriesFts(
    @ColumnInfo(name = "Name")
    val name: String,
)