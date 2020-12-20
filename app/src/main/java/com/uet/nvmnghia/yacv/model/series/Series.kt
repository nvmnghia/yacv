package com.uet.nvmnghia.yacv.model.series

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Series(
    @ColumnInfo(name = COLUMN_SERIES_NAME)
    val name: String,
    @ColumnInfo(name = "Volume")
    var volume: Int? = null,
    @ColumnInfo(name = "Count")
    var count: Int? = null,
    @ColumnInfo(name = "IsManga")
    var manga: Boolean? = null
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_SERIES_ID)
    var id: Long = 0

    companion object {
        const val COLUMN_SERIES_ID = "SeriesID"

        internal const val COLUMN_SERIES_NAME = "Name"
    }
}