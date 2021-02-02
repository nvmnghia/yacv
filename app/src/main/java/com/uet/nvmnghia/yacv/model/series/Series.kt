package com.uet.nvmnghia.yacv.model.series

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uet.nvmnghia.yacv.model.search.SearchableMetadata


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
) : SearchableMetadata {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_SERIES_ID)
    var id: Long = 0

    override fun getID() = id

    override fun getLabel() = name

    companion object {
        const val COLUMN_SERIES_ID = "SeriesID"

        internal const val COLUMN_SERIES_NAME = "Name"
    }

}